package br.com.uolcompass.entrypoints.dto;

import br.com.uolcompass.core.enums.TransferenceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "A single transference entry in the wallet statement")
public record StatementItem(

        @Schema(description = "Transference unique identifier")
        Long id,

        @Schema(description = "Direction: SENT or RECEIVED", example = "SENT")
        String type,

        @Schema(description = "Counterpart wallet name", example = "Milton")
        String counterparty,

        @Schema(description = "Transferred amount", example = "150.00")
        BigDecimal amount,

        @Schema(description = "When the transference was created")
        LocalDateTime createdAt,

        @Schema(description = "Current status")
        TransferenceStatus status
) {}
