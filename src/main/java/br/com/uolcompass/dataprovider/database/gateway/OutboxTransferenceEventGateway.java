package br.com.uolcompass.dataprovider.database.gateway;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.gateway.TransferenceEventGateway;
import br.com.uolcompass.dataprovider.database.entity.OutboxEventEntity;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceInitiatedEvent;
import br.com.uolcompass.dataprovider.repository.OutboxEventRepository;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class OutboxTransferenceEventGateway implements TransferenceEventGateway {

    private static final Logger log = LoggerFactory.getLogger(OutboxTransferenceEventGateway.class);

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void publishInitiated(TransferenceDomain transference) {
        var event = new TransferenceInitiatedEvent(
                transference.getId(),
                transference.getPayerId(),
                transference.getPayeeId(),
                transference.getAmount()
        );

        try {
            var payload = objectMapper.writeValueAsString(event);
            var outboxEvent = new OutboxEventEntity();
            outboxEvent.setRoutingKey(TransferenceRabbitMQConfig.TransferenceQueue.INITIATED.queueName);
            outboxEvent.setPayload(payload);
            outboxEvent.setPublished(false);
            outboxEvent.setCreatedAt(LocalDateTime.now(ZoneId.of("UTC-3")));

            outboxEventRepository.save(outboxEvent);

            log.info("outbox_event_saved transferenceId={} routingKey={}",
                    transference.getId(), outboxEvent.getRoutingKey());
        } catch (JsonProcessingException ex) {
            throw new RuntimeException("Failed to serialize outbox event", ex);
        }
    }
}
