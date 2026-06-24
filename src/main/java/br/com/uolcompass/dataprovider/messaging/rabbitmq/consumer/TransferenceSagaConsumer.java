package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.dataprovider.cache.IdempotencyService;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceDebitedEvent;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceFailedEvent;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceInitiatedEvent;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class TransferenceSagaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TransferenceSagaConsumer.class);

    private static final String STEP = "debit";

    private final WalletGateway walletGateway;
    private final TransferenceGateway transferenceGateway;
    private final RabbitTemplate rabbitTemplate;
    private final IdempotencyService idempotencyService;

    @RabbitListener(queues = TransferenceRabbitMQConfig.QUEUE_INITIATED)
    @Transactional
    public void handleDebit(TransferenceInitiatedEvent event) {
        var transferenceId = event.transferenceId();

        if (idempotencyService.isProcessed(transferenceId, STEP)) {
            log.info("saga_debit_duplicate transferenceId={}", transferenceId);
            return;
        }

        log.info("saga_debit_started transferenceId={}", transferenceId);

        try {
            var payer = walletGateway.findById(event.payerId())
                    .orElseThrow(() -> new IllegalStateException("Payer not found: " + event.payerId()));

            if (payer.getType() != WalletType.INDIVIDUAL) {
                throw new IllegalStateException("Business wallet cannot transfer: " + event.payerId());
            }

            var newBalance = payer.getBalance().subtract(event.amount());
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Insufficient balance for wallet: " + event.payerId());
            }

            walletGateway.updateBalance(event.payerId(), newBalance);
            transferenceGateway.updateStatus(transferenceId, TransferenceStatus.DEBITED);

            idempotencyService.markProcessed(transferenceId, STEP);

            var debitedEvent = new TransferenceDebitedEvent(
                    transferenceId, event.payerId(), event.payeeId(), event.amount()
            );
            rabbitTemplate.convertAndSend(
                    TransferenceRabbitMQConfig.EXCHANGE,
                    TransferenceRabbitMQConfig.TransferenceQueue.DEBITED.queueName,
                    debitedEvent
            );

            log.info("saga_debit_completed transferenceId={}", transferenceId);

        } catch (Exception ex) {
            log.error("saga_debit_failed transferenceId={} reason={}", transferenceId, ex.getMessage());

            var failedEvent = new TransferenceFailedEvent(
                    transferenceId, event.payerId(), event.payeeId(), event.amount(),
                    TransferenceStatus.PENDING, ex.getMessage()
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
