package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.dataprovider.database.entity.WalletEntity;
import br.com.uolcompass.dataprovider.database.entity.TransferenceEntity;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceInitiatedEvent;
import br.com.uolcompass.dataprovider.repository.TransferenceRepository;
import br.com.uolcompass.dataprovider.repository.WalletRepository;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.instancio.Instancio.of;
import static org.instancio.Select.field;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("TransferenceSagaIT — fluxo completo MySQL + RabbitMQ")
class TransferenceSagaIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransferenceRepository transferenceRepository;

    private Long payerId;
    private Long payeeId;
    private Long transferenceId;

    @BeforeEach
    void setUp() {
        transferenceRepository.deleteAll();
        walletRepository.deleteAll();

        var payer = of(WalletEntity.class)
                .ignore(field(WalletEntity::getId))
                .set(field(WalletEntity::getCpfCnpj), "11111111111")
                .set(field(WalletEntity::getBalance), BigDecimal.valueOf(1000))
                .set(field(WalletEntity::getType), WalletType.INDIVIDUAL)
                .set(field(WalletEntity::getVersion), 0L)
                .create();
        payerId = walletRepository.save(payer).getId();

        var payee = of(WalletEntity.class)
                .ignore(field(WalletEntity::getId))
                .set(field(WalletEntity::getCpfCnpj), "22222222222")
                .set(field(WalletEntity::getBalance), BigDecimal.valueOf(500))
                .set(field(WalletEntity::getType), WalletType.INDIVIDUAL)
                .set(field(WalletEntity::getVersion), 0L)
                .create();
        payeeId = walletRepository.save(payee).getId();

        var transference = new TransferenceEntity();
        transference.setPayer(walletRepository.getReferenceById(payerId));
        transference.setPayee(walletRepository.getReferenceById(payeeId));
        transference.setAmount(BigDecimal.valueOf(150));
        transference.setStatus(TransferenceStatus.PENDING);
        transferenceId = transferenceRepository.save(transference).getId();
    }

    @AfterEach
    void tearDown() {
        transferenceRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    @DisplayName("Should complete full SAGA: initiated → debited → completed")
    void shouldCompleteFullSagaSuccessfully() {
        var event = new TransferenceInitiatedEvent(
                transferenceId, payerId, payeeId, BigDecimal.valueOf(150));

        rabbitTemplate.convertAndSend(
                TransferenceRabbitMQConfig.EXCHANGE,
                TransferenceRabbitMQConfig.TransferenceQueue.INITIATED.queueName,
                event);

        await().untilAsserted(() -> {
            var updatedPayer = walletRepository.findById(payerId).orElseThrow();
            assertThat(updatedPayer.getBalance())
                    .isEqualByComparingTo(BigDecimal.valueOf(850));

            var updatedPayee = walletRepository.findById(payeeId).orElseThrow();
            assertThat(updatedPayee.getBalance())
                    .isEqualByComparingTo(BigDecimal.valueOf(650));

            var transference = transferenceRepository.findById(transferenceId).orElseThrow();
            assertThat(transference.getStatus()).isEqualTo(TransferenceStatus.COMPLETED);
        });
    }
}
