package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.dataprovider.cache.IdempotencyService;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceCompletedEvent;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceDebitedEvent;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferenceCreditConsumerTest {

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private TransferenceGateway transferenceGateway;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private TransferenceCreditConsumer consumer;

    private TransferenceDebitedEvent event;
    private WalletDomain payee;

    @BeforeEach
    void setUp() {
        event = new TransferenceDebitedEvent(1L, 10L, 20L, BigDecimal.valueOf(100));
        payee = new WalletDomain(20L, "Milton", "456", "miltom@test.com",
                "pass", BigDecimal.valueOf(200), WalletType.INDIVIDUAL, 0L);
    }

    @Test
    void shouldCreditSuccessfully() {
        when(idempotencyService.isProcessed(1L, "credit")).thenReturn(false);
        when(walletGateway.findById(20L)).thenReturn(Optional.of(payee));

        consumer.handleCredit(event);

        verify(walletGateway).updateBalance(20L, BigDecimal.valueOf(300));
        verify(transferenceGateway).updateStatus(1L, TransferenceStatus.COMPLETED);
        verify(idempotencyService).markProcessed(1L, "credit");

        var captor = ArgumentCaptor.forClass(TransferenceCompletedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(TransferenceRabbitMQConfig.EXCHANGE),
                eq("transference.completed"),
                captor.capture()
        );
        assertThat(captor.getValue().transferenceId()).isEqualTo(1L);
    }

    @Test
    void shouldSkipWhenAlreadyProcessed() {
        when(idempotencyService.isProcessed(1L, "credit")).thenReturn(true);

        consumer.handleCredit(event);

        verify(walletGateway, never()).findById(any());
        verify(transferenceGateway, never()).updateStatus(any(), any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void shouldPublishFailedEventOnError() {
        when(idempotencyService.isProcessed(1L, "credit")).thenReturn(false);
        when(walletGateway.findById(20L)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> consumer.handleCredit(event))
                .isInstanceOf(RuntimeException.class);

        verify(rabbitTemplate).convertAndSend(
                eq(TransferenceRabbitMQConfig.EXCHANGE),
                eq("transference.failed"),
                any(Object.class)
        );
    }
}
