package br.com.uolcompass.dataprovider.cache;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.gateway.WalletGateway;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Primary
@Component
@RequiredArgsConstructor
public class WalletCacheAdapter implements WalletGateway {

    private static final Logger log = LoggerFactory.getLogger(WalletCacheAdapter.class);

    private static final String WALLET_KEY_PREFIX  = "wallet:";
    private static final String BALANCE_KEY_PREFIX = "balance:";
    private static final long WALLET_TTL_SECONDS   = 300;
    private static final long BALANCE_TTL_SECONDS  = 30;

    private static final ObjectMapper OBJECT_MAPPER = createObjectMapper();

    private final WalletGateway delegate;
    private final StringRedisTemplate redisTemplate;

    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Override
    public WalletDomain create(WalletDomain walletDomain) {
        return delegate.create(walletDomain);
    }

    @Override
    public boolean existsByCpfCnpj(String cpfCnpj) {
        return delegate.existsByCpfCnpj(cpfCnpj);
    }

    @Override
    public Optional<WalletDomain> findById(Long id) {
        var cacheKey = WALLET_KEY_PREFIX + id;

        var cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("cache_hit_wallet walletId={}", id);
            try {
                return Optional.of(OBJECT_MAPPER.readValue(cached, WalletDomain.class));
            } catch (JsonProcessingException ex) {
                log.warn("cache_deserialize_error walletId={}", id, ex);
                redisTemplate.delete(cacheKey);
            }
        }

        log.debug("cache_miss_wallet walletId={}", id);
        var result = delegate.findById(id);

        result.ifPresent(wallet -> {
            try {
                redisTemplate.opsForValue().set(
                        cacheKey,
                        OBJECT_MAPPER.writeValueAsString(wallet),
                        Duration.ofSeconds(WALLET_TTL_SECONDS)
                );
            } catch (JsonProcessingException ex) {
                log.warn("cache_serialize_error walletId={}", id, ex);
            }
        });

        return result;
    }

    @Override
    public List<WalletDomain> findAllById(Collection<Long> ids) {
        return delegate.findAllById(ids);
    }

    @Override
    public void updateBalance(Long walletId, BigDecimal newBalance) {
        invalidateCache(walletId);
        delegate.updateBalance(walletId, newBalance);
    }

    private void invalidateCache(Long walletId) {
        redisTemplate.delete(WALLET_KEY_PREFIX + walletId);
        redisTemplate.delete(BALANCE_KEY_PREFIX + walletId);
        log.debug("cache_invalidated walletId={}", walletId);
    }
}
