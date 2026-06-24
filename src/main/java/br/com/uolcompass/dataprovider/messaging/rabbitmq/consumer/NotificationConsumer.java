package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.dataprovider.cache.IdempotencyService;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceCompletedEvent;
import br.com.uolcompass.entrypoints.config.messaging.NotificationRabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    private static final String STEP = "notification";
    private static final String NOTIFY_URL = "https://util.devi.tools/api/v1/notify";
    private static final String REDIS_KEY_PREFIX = "notification:sent:";
    private static final long REDIS_TTL_HOURS = 168;

    private final RestTemplate restTemplate;
    private final StringRedisTemplate redisTemplate;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = NotificationRabbitMQConfig.SEND_QUEUE)
    public void handleNotification(TransferenceCompletedEvent event) {
        var transferenceId = event.transferenceId();

        if (idempotencyService.isProcessed(transferenceId, STEP)) {
            log.info("notification_duplicate transferenceId={}", transferenceId);
            return;
        }

        log.info("notification_started transferenceId={}", transferenceId);

        try {
            var response = restTemplate.postForEntity(NOTIFY_URL, null, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                var redisKey = REDIS_KEY_PREFIX + transferenceId;
                redisTemplate.opsForValue().set(redisKey, "sent", Duration.ofHours(REDIS_TTL_HOURS));

                idempotencyService.markProcessed(transferenceId, STEP);

                log.info("notification_sent transferenceId={} payeeId={} amount={}",
                        transferenceId, event.payeeId(), event.amount());
            } else {
                log.warn("notification_api_failed transferenceId={} status={}",
                        transferenceId, response.getStatusCode());
                throw new IllegalStateException("Notification API returned " + response.getStatusCode());
            }

        } catch (Exception ex) {
            log.error("notification_failed transferenceId={} reason={}",
                    transferenceId, ex.getMessage());
            throw ex;
        }
    }
}
