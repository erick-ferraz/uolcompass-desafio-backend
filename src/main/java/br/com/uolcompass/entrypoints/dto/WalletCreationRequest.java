package br.com.uolcompass.entrypoints.dto;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.WalletType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request payload for creating a new wallet")
public record WalletCreationRequest(
        @NotBlank
        @Schema(description = "Full name of the wallet owner", example = "John Doe")
        String name,

        @NotBlank
        @Schema(description = "CPF or CNPJ (numbers only)", example = "12345678901")
        String cpfCnpj,

        @NotBlank @Email
        @Schema(description = "Email address", example = "john@example.com")
        String email,

        @NotBlank
        @Schema(description = "Password (plain text)", example = "securePass123")
        String password,

        @NotNull
        @Schema(description = "Wallet type", example = "USER")
        WalletType type
) {

        public WalletDomain toDomain() {
                return new WalletDomain(
                        null,
                        this.name,
                        this.cpfCnpj,
                        this.email,
                        this.password,
                        BigDecimal.ZERO,
                        this.type
                );
        }
}
