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
@DisplayName("BusinessWalletTransferIT — rejeição de carteira BUSINESS")
class BusinessWalletTransferIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransferenceRepository transferenceRepository;

    private Long businessWalletId;
    private Long payeeId;
    private Long transferenceId;

    @BeforeEach
    void setUp() {
        transferenceRepository.deleteAll();
        walletRepository.deleteAll();

        var business = of(WalletEntity.class)
                .ignore(field(WalletEntity::getId))
                .set(field(WalletEntity::getCpfCnpj), "11222333000181")
                .set(field(WalletEntity::getBalance), BigDecimal.valueOf(10000))
                .set(field(WalletEntity::getType), WalletType.BUSINESS)
                .set(field(WalletEntity::getVersion), 0L)
                .create();
        businessWalletId = walletRepository.save(business).getId();

        var payee = of(WalletEntity.class)
                .ignore(field(WalletEntity::getId))
                .set(field(WalletEntity::getCpfCnpj), "33344455566")
                .set(field(WalletEntity::getBalance), BigDecimal.ZERO)
                .set(field(WalletEntity::getType), WalletType.INDIVIDUAL)
                .set(field(WalletEntity::getVersion), 0L)
                .create();
        payeeId = walletRepository.save(payee).getId();

        var transference = new TransferenceEntity();
        transference.setPayer(walletRepository.getReferenceById(businessWalletId));
        transference.setPayee(walletRepository.getReferenceById(payeeId));
        transference.setAmount(BigDecimal.valueOf(100));
        transference.setStatus(TransferenceStatus.PENDING);
        transferenceId = transferenceRepository.save(transference).getId();
    }

    @AfterEach
    void tearDown() {
        transferenceRepository.deleteAll();
        walletRepository.deleteAll();
    }

    @Test
    @DisplayName("Should reject transfer from BUSINESS wallet → COMPENSATED")
    void shouldRejectTransferFromBusinessWallet() {
        var event = new TransferenceInitiatedEvent(
                transferenceId, businessWalletId, payeeId, BigDecimal.valueOf(100));

        rabbitTemplate.convertAndSend(
                TransferenceRabbitMQConfig.EXCHANGE,
                TransferenceRabbitMQConfig.TransferenceQueue.INITIATED.queueName,
                event);

        await().untilAsserted(() -> {
            var payer = walletRepository.findById(businessWalletId).orElseThrow();
            assertThat(payer.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(10000));

            var transference = transferenceRepository.findById(transferenceId).orElseThrow();
            assertThat(transference.getStatus()).isEqualTo(TransferenceStatus.COMPENSATED);
        });
    }
}
