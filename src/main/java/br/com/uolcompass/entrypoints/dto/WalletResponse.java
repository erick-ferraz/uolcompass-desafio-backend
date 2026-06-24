package br.com.uolcompass.entrypoints.dto;

import br.com.uolcompass.core.enums.WalletType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Response payload for wallet operations")
public record WalletResponse(

        @Schema(description = "Wallet unique identifier", example = "1")
        Long id,

        @Schema(description = "Full name", example = "John Doe")
        String name,

        @Schema(description = "CPF or CNPJ (numbers only)", example = "12345678901")
        String cpfCnpj,

        @Schema(description = "Email address", example = "john@example.com")
        String email,

        @Schema(description = "Current balance", example = "2500.00")
        BigDecimal balance,

        @Schema(description = "Wallet type", example = "INDIVIDUAL")
        WalletType type,

        @Schema(description = "When the wallet was created", example = "2026-01-15T10:30:00")
        LocalDateTime createdAt
) {
}
