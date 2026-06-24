package br.com.uolcompass.dataprovider.cache;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class IdempotencyService {

    private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

    private static final String KEY_PREFIX = "idempotency:";
    private static final long TTL_SECONDS = 86400;

    private final StringRedisTemplate redisTemplate;

    public boolean isProcessed(Long transferenceId, String step) {
        var key = buildKey(transferenceId, step);
        var exists = redisTemplate.hasKey(key);
        if (Boolean.TRUE.equals(exists)) {
            log.info("idempotency_hit transferenceId={} step={}", transferenceId, step);
            return true;
        }
        return false;
    }

    public void markProcessed(Long transferenceId, String step) {
        var key = buildKey(transferenceId, step);
        redisTemplate.opsForValue().set(key, "processed", TTL_SECONDS, TimeUnit.SECONDS);
        log.debug("idempotency_mark transferenceId={} step={}", transferenceId, step);
    }

    private String buildKey(Long transferenceId, String step) {
        return KEY_PREFIX + transferenceId + ":" + step;
    }
}
