package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.TransferenceEventGateway;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InitiateTransferenceUseCaseImplTest {

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private TransferenceGateway transferenceGateway;

    @Mock
    private TransferenceEventGateway transferenceEventGateway;

    @InjectMocks
    private InitiateTransferenceUseCaseImpl useCase;

    private WalletDomain payer;
    private WalletDomain payee;
    private TransferenceDomain input;

    @BeforeEach
    void setUp() {
        payer = new WalletDomain(1L, "John", "123", "john@test.com",
                "pass", BigDecimal.valueOf(500), WalletType.INDIVIDUAL, 0L);
        payee = new WalletDomain(2L, "Milton", "456", "milton@test.com",
                "pass", BigDecimal.ZERO, WalletType.INDIVIDUAL, 0L);
        input = new TransferenceDomain(null, 1L, 2L, BigDecimal.valueOf(100), null);
    }

    @Test
    void shouldInitiateTransferenceSuccessfully() {
        when(walletGateway.findById(1L)).thenReturn(Optional.of(payer));
        when(walletGateway.findById(2L)).thenReturn(Optional.of(payee));
        when(transferenceGateway.create(any())).thenAnswer(inv -> {
            var domain = inv.getArgument(0, TransferenceDomain.class);
            return new TransferenceDomain(10L, domain.getPayerId(), domain.getPayeeId(),
                    domain.getAmount(), TransferenceStatus.PENDING);
        });

        var result = useCase.execute(input);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo(TransferenceStatus.PENDING);
        verify(transferenceEventGateway).publishInitiated(any());
    }

    @Test
    void shouldThrowExceptionWhenPayerIsBusiness() {
        payer = new WalletDomain(1L, "Biz", "12", "biz@test.com",
                "pass", BigDecimal.valueOf(500), WalletType.BUSINESS, 0L);
        when(walletGateway.findById(1L)).thenReturn(Optional.of(payer));
        when(walletGateway.findById(2L)).thenReturn(Optional.of(payee));

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(BusinessWalletCannotTransferException.class);

        verify(transferenceGateway, never()).create(any());
        verify(transferenceEventGateway, never()).publishInitiated(any());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        payer = new WalletDomain(1L, "John", "123", "john@test.com",
                "pass", BigDecimal.valueOf(50), WalletType.INDIVIDUAL, 0L);
        when(walletGateway.findById(1L)).thenReturn(Optional.of(payer));
        when(walletGateway.findById(2L)).thenReturn(Optional.of(payee));

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(InsufficientBalanceException.class);

        verify(transferenceGateway, never()).create(any());
        verify(transferenceEventGateway, never()).publishInitiated(any());
    }

    @Test
    void shouldThrowExceptionWhenSameWallet() {
        input = new TransferenceDomain(null, 1L, 1L, BigDecimal.valueOf(100), null);
        when(walletGateway.findById(1L)).thenReturn(Optional.of(payer));

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(SameWalletTransferException.class);

        verify(transferenceGateway, never()).create(any());
        verify(transferenceEventGateway, never()).publishInitiated(any());
    }

    @Test
    void shouldThrowExceptionWhenPayerNotFound() {
        when(walletGateway.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(WalletNotFoundException.class);
    }

    @Test
    void shouldThrowExceptionWhenPayeeNotFound() {
        when(walletGateway.findById(1L)).thenReturn(Optional.of(payer));
        when(walletGateway.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(WalletNotFoundException.class);
    }
}
