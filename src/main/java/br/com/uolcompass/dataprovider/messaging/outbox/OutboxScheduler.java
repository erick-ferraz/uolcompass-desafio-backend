package br.com.uolcompass.dataprovider.messaging.outbox;

import br.com.uolcompass.dataprovider.repository.OutboxEventRepository;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishPendingEvents() {
        var pendingEvents = outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("outbox_polling pendingEvents={}", pendingEvents.size());

        for (var event : pendingEvents) {
            try {
                var message = MessageBuilder.withBody(event.getPayload().getBytes(StandardCharsets.UTF_8))
                        .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                        .build();

                rabbitTemplate.send(TransferenceRabbitMQConfig.EXCHANGE, event.getRoutingKey(), message);

                event.setPublished(true);
                outboxEventRepository.save(event);

                log.info("outbox_event_published outboxEventId={} routingKey={}",
                        event.getId(), event.getRoutingKey());
            } catch (Exception ex) {
                log.error("outbox_publish_failed outboxEventId={} routingKey={} reason={}",
                        event.getId(), event.getRoutingKey(), ex.getMessage());
            }
        }
    }
}
