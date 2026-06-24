package br.com.uolcompass.dataprovider.messaging.rabbitmq.consumer;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.dataprovider.cache.IdempotencyService;
import br.com.uolcompass.dataprovider.messaging.dto.TransferenceFailedEvent;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompensationConsumerTest {

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private TransferenceGateway transferenceGateway;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private IdempotencyService idempotencyService;

    @InjectMocks
    private CompensationConsumer consumer;

    private WalletDomain payer;

    @BeforeEach
    void setUp() {
        payer = new WalletDomain(10L, "John", "123", "john@test.com",
                "pass", BigDecimal.valueOf(300), WalletType.INDIVIDUAL, 0L, null);
    }

    @Test
    void shouldReverseDebitWhenStatusIsDebited() {
        var event = new TransferenceFailedEvent(1L, 10L, 20L,
                BigDecimal.valueOf(100), TransferenceStatus.DEBITED, "credit failed");

        when(idempotencyService.isProcessed(1L, "compensation")).thenReturn(false);
        when(walletGateway.findById(10L)).thenReturn(Optional.of(payer));

        consumer.handleCompensation(event);

        verify(walletGateway).updateBalance(10L, BigDecimal.valueOf(400));
        verify(transferenceGateway).updateStatus(1L, TransferenceStatus.COMPENSATED);
        verify(idempotencyService).markProcessed(1L, "compensation");
    }

    @Test
    void shouldSkipReverseWhenStatusIsNotDebited() {
        var event = new TransferenceFailedEvent(1L, 10L, 20L,
                BigDecimal.valueOf(100), TransferenceStatus.PENDING, "validation failed");

        when(idempotencyService.isProcessed(1L, "compensation")).thenReturn(false);

        consumer.handleCompensation(event);

        verify(walletGateway, never()).updateBalance(any(), any());
        verify(transferenceGateway).updateStatus(1L, TransferenceStatus.COMPENSATED);
        verify(idempotencyService).markProcessed(1L, "compensation");
    }

    @Test
    void shouldSkipWhenAlreadyProcessed() {
        var event = new TransferenceFailedEvent(1L, 10L, 20L,
                BigDecimal.valueOf(100), TransferenceStatus.DEBITED, "error");

        when(idempotencyService.isProcessed(1L, "compensation")).thenReturn(true);

        consumer.handleCompensation(event);

        verify(walletGateway, never()).findById(any());
        verify(transferenceGateway, never()).updateStatus(any(), any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void shouldRethrowException() {
        var event = new TransferenceFailedEvent(1L, 10L, 20L,
                BigDecimal.valueOf(100), TransferenceStatus.DEBITED, "error");

        when(idempotencyService.isProcessed(1L, "compensation")).thenReturn(false);
        when(walletGateway.findById(10L)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> consumer.handleCompensation(event))
                .isInstanceOf(RuntimeException.class);
    }
}
