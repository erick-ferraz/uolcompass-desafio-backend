package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceCompensatedEvent;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceFailedEvent;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class CompensationConsumer {

    private static final Logger log = LoggerFactory.getLogger(CompensationConsumer.class);

    private final WalletGateway walletGateway;
    private final TransferenceGateway transferenceGateway;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = TransferenceRabbitMQConfig.QUEUE_FAILED)
    @Transactional
    public void handleCompensation(TransferenceFailedEvent event) {
        var transferenceId = event.transferenceId();
        log.info("saga_compensation_started transferenceId={} currentStatus={}",
                transferenceId, event.currentStatus());

        try {
            if (event.currentStatus() == TransferenceStatus.DEBITED) {
                var payer = walletGateway.findById(event.payerId())
                        .orElseThrow(() -> new IllegalStateException("Payer not found: " + event.payerId()));

                var reversedBalance = payer.getBalance().add(event.amount());
                walletGateway.updateBalance(event.payerId(), reversedBalance);

                log.info("saga_compensation_reversed transferenceId={} payerId={} amount={}",
                        transferenceId, event.payerId(), event.amount());
            }

            transferenceGateway.updateStatus(transferenceId, TransferenceStatus.COMPENSATED);

            var compensatedEvent = new TransferenceCompensatedEvent(
                    transferenceId, event.payerId(), event.payeeId(), event.amount()
            );
            rabbitTemplate.convertAndSend(
                    TransferenceRabbitMQConfig.EXCHANGE,
                    TransferenceRabbitMQConfig.TransferenceQueue.COMPENSATED.queueName,
                    compensatedEvent
            );

            log.info("saga_compensation_completed transferenceId={}", transferenceId);

        } catch (Exception ex) {
            log.error("saga_compensation_failed transferenceId={} reason={}",
                    transferenceId, ex.getMessage());
            throw ex;
        }
    }
}
