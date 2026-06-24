package br.com.uolcompass.dataprovider.database.gateway;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.dataprovider.database.entity.OutboxEventEntity;
import br.com.uolcompass.dataprovider.repository.OutboxEventRepository;
import br.com.uolcompass.entrypoints.config.messaging.TransferenceRabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxTransferenceEventGatewayTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OutboxTransferenceEventGateway gateway;

    @Test
    void shouldPersistOutboxEvent() {
        var transference = new TransferenceDomain(1L, 10L, 20L,
                BigDecimal.valueOf(100), TransferenceStatus.PENDING);

        gateway.publishInitiated(transference);

        var captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository).save(captor.capture());

        var saved = captor.getValue();
        assertThat(saved.getRoutingKey())
                .isEqualTo(TransferenceRabbitMQConfig.TransferenceQueue.INITIATED.queueName);
        assertThat(saved.getPublished()).isFalse();
        assertThat(saved.getPayload()).contains("\"transferenceId\":1");
    }
}
