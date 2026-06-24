package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.dataprovider.cache.IdempotencyService;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceDebitedEvent;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceInitiatedEvent;
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
class TransferenceSagaConsumerTest {

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private TransferenceGateway transferenceGateway;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private TransferenceSagaConsumer consumer;

    private TransferenceInitiatedEvent event;
    private WalletDomain payer;

    @BeforeEach
    void setUp() {
        event = new TransferenceInitiatedEvent(1L, 10L, 20L, BigDecimal.valueOf(100));
        payer = new WalletDomain(10L, "John", "123", "john@test.com",
                "pass", BigDecimal.valueOf(500), WalletType.INDIVIDUAL, 0L, null);
    }

    @Test
    void shouldDebitSuccessfully() {
        when(idempotencyService.isProcessed(1L, "debit")).thenReturn(false);
        when(walletGateway.findById(10L)).thenReturn(Optional.of(payer));

        consumer.handleDebit(event);

        verify(walletGateway).updateBalance(10L, BigDecimal.valueOf(400));
        verify(transferenceGateway).updateStatus(1L, TransferenceStatus.DEBITED);
        verify(idempotencyService).markProcessed(1L, "debit");

        var captor = ArgumentCaptor.forClass(TransferenceDebitedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(TransferenceRabbitMQConfig.EXCHANGE),
                eq("transference.debited"),
                captor.capture()
        );
        assertThat(captor.getValue().transferenceId()).isEqualTo(1L);
    }

    @Test
    void shouldSkipWhenAlreadyProcessed() {
        when(idempotencyService.isProcessed(1L, "debit")).thenReturn(true);

        consumer.handleDebit(event);

        verify(walletGateway, never()).findById(any());
        verify(transferenceGateway, never()).updateStatus(any(), any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void shouldThrowExceptionWhenPayerNotFound() {
        when(idempotencyService.isProcessed(1L, "debit")).thenReturn(false);
        when(walletGateway.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consumer.handleDebit(event))
                .isInstanceOf(IllegalStateException.class);

        verify(transferenceGateway, never()).updateStatus(any(), any());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        payer = new WalletDomain(10L, "John", "123", "john@test.com",
                "pass", BigDecimal.valueOf(50), WalletType.INDIVIDUAL, 0L, null);
        when(idempotencyService.isProcessed(1L, "debit")).thenReturn(false);
        when(walletGateway.findById(10L)).thenReturn(Optional.of(payer));

        assertThatThrownBy(() -> consumer.handleDebit(event))
                .isInstanceOf(IllegalStateException.class);

        verify(transferenceGateway, never()).updateStatus(1L, TransferenceStatus.DEBITED);
    }

    @Test
    void shouldPublishFailedEventOnError() {
        when(idempotencyService.isProcessed(1L, "debit")).thenReturn(false);
        when(walletGateway.findById(10L)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> consumer.handleDebit(event))
                .isInstanceOf(RuntimeException.class);

        verify(rabbitTemplate).convertAndSend(
                eq(TransferenceRabbitMQConfig.EXCHANGE),
                eq("transference.failed"),
                any(Object.class)
        );
    }
}
