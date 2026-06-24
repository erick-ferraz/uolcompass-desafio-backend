package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransferenceStatusUseCaseImplTest {

    @Mock
    private TransferenceGateway transferenceGateway;

    @InjectMocks
    private GetTransferenceStatusUseCaseImpl useCase;

    @Test
    void shouldReturnTransferenceWhenFound() {
        var domain = new TransferenceDomain(1L, 1L, 2L,
                BigDecimal.valueOf(100), TransferenceStatus.PENDING);
        when(transferenceGateway.findById(1L)).thenReturn(Optional.of(domain));

        var result = useCase.execute(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(1L);
        assertThat(result.get().getStatus()).isEqualTo(TransferenceStatus.PENDING);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        when(transferenceGateway.findById(99L)).thenReturn(Optional.empty());

        var result = useCase.execute(99L);

        assertThat(result).isEmpty();
    }
}
