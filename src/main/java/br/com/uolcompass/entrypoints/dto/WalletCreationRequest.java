package br.com.uolcompass.entrypoints.dto;

import br.com.uolcompass.core.enums.WalletType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
}
