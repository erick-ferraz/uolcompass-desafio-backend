package br.com.uolcompass.dataprovider.cache;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.WalletGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("WalletCacheAdapterIT — cache Redis")
class WalletCacheAdapterIT {

    @Autowired
    private WalletGateway walletGateway;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private Long walletId;

    @BeforeEach
    void setUp() {
        var wallet = of(WalletDomain.class)
                .set(field(WalletDomain::getId), null)
                .set(field(WalletDomain::getType), WalletType.INDIVIDUAL)
                .set(field(WalletDomain::getBalance), BigDecimal.valueOf(1000))
                .create();
        walletId = walletGateway.create(wallet).getId();
        redisTemplate.delete("wallet:" + walletId);
        redisTemplate.delete("balance:" + walletId);
    }

    @Test
    @DisplayName("Should cache wallet after first fetch (cache miss → populate)")
    void shouldCacheWalletAfterFirstFetch() {
        var firstFetch = walletGateway.findById(walletId);
        assertThat(firstFetch).isPresent();

        var cached = redisTemplate.opsForValue().get("wallet:" + walletId);
        assertThat(cached).isNotNull();
        assertThat(cached).contains("\"id\":" + walletId);
    }

    @Test
    @DisplayName("Should return from cache on second fetch (cache hit)")
    void shouldReturnFromCacheOnSecondFetch() {
        walletGateway.findById(walletId);
        walletGateway.findById(walletId);

        var cached = redisTemplate.opsForValue().get("wallet:" + walletId);
        assertThat(cached).isNotNull();
    }

    @Test
    @DisplayName("Should invalidate cache when balance is updated")
    void shouldInvalidateCacheOnBalanceUpdate() {
        walletGateway.findById(walletId);

        walletGateway.updateBalance(walletId, BigDecimal.valueOf(500));

        var cached = redisTemplate.opsForValue().get("wallet:" + walletId);
        assertThat(cached).isNull();
    }

    @Test
    @DisplayName("Should handle corrupted cache gracefully (fallback to DB)")
    void shouldHandleCorruptedCacheGracefully() {
        redisTemplate.opsForValue().set("wallet:" + walletId, "invalid json");

        var result = walletGateway.findById(walletId);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(walletId);
    }
}
