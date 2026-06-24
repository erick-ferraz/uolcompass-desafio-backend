package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.WalletGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseImplTest {

    @Mock
    private WalletGateway walletGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateWalletUseCaseImpl useCase;

    private final WalletDomain input = new WalletDomain(
            null, "John Doe", "12345678901", "john@example.com",
            "rawPass123", BigDecimal.ZERO, WalletType.INDIVIDUAL, null
    );

    @Test
    void shouldCreateWalletWithHashedPassword() {
        when(passwordEncoder.encode("rawPass123")).thenReturn("$2a$10$hashedValue");
        when(walletGateway.create(any())).thenAnswer(inv -> inv.getArgument(0));
        when(walletGateway.existsByCpfCnpj(anyString())).thenReturn(false);

        var result = useCase.execute(input);

        var captor = ArgumentCaptor.forClass(WalletDomain.class);
        verify(walletGateway).create(captor.capture());

        var captured = captor.getValue();
        assertThat(captured.getPassword()).isEqualTo("$2a$10$hashedValue");
    }

    @Test
    void shouldThrowExceptionWhenCpfCnpjAlreadyExists() {
        when(walletGateway.existsByCpfCnpj("12345678901")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(CpfCnpjAlreadyExistsException.class)
                .hasMessageContaining("12345678901");

        verify(walletGateway, never()).create(any());
    }
}
