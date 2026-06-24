package br.com.uolcompass.dataprovider.messaging.rabbitmq.producer;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.gateway.TransferenceEventGateway;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceInitiatedEvent;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferenceEventProducer implements TransferenceEventGateway {

    private static final Logger log = LoggerFactory.getLogger(TransferenceEventProducer.class);

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishInitiated(TransferenceDomain transference) {
        var event = new TransferenceInitiatedEvent(
                transference.getId(),
                transference.getPayerId(),
                transference.getPayeeId(),
                transference.getAmount()
        );
        log.info("publish_initiated transferenceId={}", transference.getId());
        rabbitTemplate.convertAndSend(
                TransferenceRabbitMQConfig.EXCHANGE,
                TransferenceRabbitMQConfig.TransferenceQueue.INITIATED.queueName,
                event
        );
    }
}
