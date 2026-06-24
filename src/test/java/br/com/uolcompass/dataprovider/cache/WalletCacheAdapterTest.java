package br.com.uolcompass.dataprovider.cache;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.WalletGateway;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WalletCacheAdapterTest {

    @Mock
    private WalletGateway delegate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @InjectMocks
    private WalletCacheAdapter adapter;

    private final ObjectMapper mapper = new ObjectMapper();
    private WalletDomain wallet;

    @BeforeEach
    void setUp() throws Exception {
        wallet = new WalletDomain(1L, "John", "123", "john@test.com",
                "pass", BigDecimal.valueOf(500), WalletType.INDIVIDUAL, 0L, null);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void shouldReturnCachedWalletWhenFound() throws Exception {
        var cachedJson = mapper.writeValueAsString(wallet);
        when(valueOps.get("wallet:1")).thenReturn(cachedJson);
        when(delegate.findById(1L)).thenReturn(Optional.of(wallet));

        var result = adapter.findById(1L);

        assertThat(result).isPresent();
    }

    @Test
    void shouldFetchFromDelegateAndCacheWhenMiss() throws Exception {
        when(valueOps.get("wallet:1")).thenReturn(null);
        when(delegate.findById(1L)).thenReturn(Optional.of(wallet));

        var result = adapter.findById(1L);

        assertThat(result).isPresent();
        verify(valueOps).set(eq("wallet:1"), anyString(), eq(Duration.ofSeconds(300)));
    }

    @Test
    void shouldReturnEmptyWhenNotInCacheOrDb() {
        when(valueOps.get("wallet:99")).thenReturn(null);
        when(delegate.findById(99L)).thenReturn(Optional.empty());

        var result = adapter.findById(99L);

        assertThat(result).isEmpty();
        verify(valueOps, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void shouldInvalidateCacheOnBalanceUpdate() {
        adapter.updateBalance(1L, BigDecimal.valueOf(100));

        verify(redisTemplate).delete("wallet:1");
        verify(redisTemplate).delete("balance:1");
        verify(delegate).updateBalance(1L, BigDecimal.valueOf(100));
    }

    @Test
    void shouldDeleteCorruptedCacheAndFetchFromDelegate() {
        when(valueOps.get("wallet:1")).thenReturn("invalid json");
        when(delegate.findById(1L)).thenReturn(Optional.of(wallet));

        var result = adapter.findById(1L);

        assertThat(result).isPresent();
        verify(redisTemplate).delete("wallet:1");
    }
}
