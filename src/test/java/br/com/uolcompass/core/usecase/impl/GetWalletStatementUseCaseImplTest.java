package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWalletStatementUseCaseImplTest {

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private TransferenceGateway transferenceGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GetWalletStatementUseCaseImpl useCase;

    private WalletDomain wallet;

    @BeforeEach
    void setUp() {
        wallet = new WalletDomain(1L, "John", "123", "john@test.com",
                "$2a$10$hashedPass", BigDecimal.valueOf(500),
                WalletType.INDIVIDUAL, 0L, null);
    }

    @Test
    void shouldReturnStatementSuccessfully() {
        when(walletGateway.findById(1L)).thenReturn(Optional.of(wallet));
        when(passwordEncoder.matches("correctPass", "$2a$10$hashedPass")).thenReturn(true);

        var transferences = List.of(
                new TransferenceDomain(1L, 1L, 2L, BigDecimal.valueOf(50), TransferenceStatus.COMPLETED),
                new TransferenceDomain(2L, 3L, 1L, BigDecimal.valueOf(200), TransferenceStatus.COMPLETED)
        );
        when(transferenceGateway.findByWalletIdOrderByCreatedAtDesc(1L)).thenReturn(transferences);

        var result = useCase.execute(1L, "correctPass");

        assertThat(result.wallet()).isEqualTo(wallet);
        assertThat(result.transferences()).hasSize(2);
        verify(passwordEncoder).matches("correctPass", "$2a$10$hashedPass");
    }

    @Test
    void shouldThrowExceptionWhenPasswordMismatch() {
        when(walletGateway.findById(1L)).thenReturn(Optional.of(wallet));
        when(passwordEncoder.matches("wrongPass", "$2a$10$hashedPass")).thenReturn(false);

        assertThatThrownBy(() -> useCase.execute(1L, "wrongPass"))
                .isInstanceOf(WalletPasswordMismatchException.class);

        verify(transferenceGateway, never()).findByWalletIdOrderByCreatedAtDesc(any());
    }

    @Test
    void shouldThrowExceptionWhenWalletNotFound() {
        when(walletGateway.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99L, "anyPass"))
                .isInstanceOf(WalletNotFoundException.class);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(transferenceGateway, never()).findByWalletIdOrderByCreatedAtDesc(any());
    }
}
