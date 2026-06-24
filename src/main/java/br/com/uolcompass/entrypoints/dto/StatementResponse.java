package br.com.uolcompass.entrypoints.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Wallet statement with transaction history")
public record StatementResponse(

        @Schema(description = "Wallet unique identifier")
        Long walletId,

        @Schema(description = "Wallet owner name")
        String walletName,

        @Schema(description = "Current balance")
        BigDecimal balance,

        @Schema(description = "List of transactions (most recent first)")
        List<StatementItem> transactions
) {}
