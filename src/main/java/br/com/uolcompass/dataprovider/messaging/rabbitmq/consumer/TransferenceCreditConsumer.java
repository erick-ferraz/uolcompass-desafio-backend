package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceCompletedEvent;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceDebitedEvent;
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
public class TransferenceCreditConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferenceCreditConsumer.class);

    private final WalletGateway walletGateway;
    private final TransferenceGateway transferenceGateway;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = TransferenceRabbitMQConfig.QUEUE_DEBITED)
    @Transactional
    public void handleCredit(TransferenceDebitedEvent event) {
        var transferenceId = event.transferenceId();
        log.info("saga_credit_started transferenceId={}", transferenceId);

        try {
            var payee = walletGateway.findById(event.payeeId())
                    .orElseThrow(() -> new IllegalStateException("Payee not found: " + event.payeeId()));

            var newBalance = payee.getBalance().add(event.amount());
            walletGateway.updateBalance(event.payeeId(), newBalance);
            transferenceGateway.updateStatus(transferenceId, TransferenceStatus.COMPLETED);

            var completedEvent = new TransferenceCompletedEvent(
                    transferenceId, event.payerId(), event.payeeId(), event.amount()
            );
            rabbitTemplate.convertAndSend(
                    TransferenceRabbitMQConfig.EXCHANGE,
                    TransferenceRabbitMQConfig.TransferenceQueue.COMPLETED.queueName,
                    completedEvent
            );

            log.info("saga_credit_completed transferenceId={}", transferenceId);

        } catch (Exception ex) {
            log.error("saga_credit_failed transferenceId={} reason={}", transferenceId, ex.getMessage());

            var failedEvent = new TransferenceFailedEvent(
                    transferenceId, event.payerId(), event.payeeId(), event.amount(),
                    TransferenceStatus.DEBITED, ex.getMessage()
            );
            rabbitTemplate.convertAndSend(
                    TransferenceRabbitMQConfig.EXCHANGE,
                    TransferenceRabbitMQConfig.TransferenceQueue.FAILED.queueName,
                    failedEvent
            );

            throw ex;
        }
    }
}
