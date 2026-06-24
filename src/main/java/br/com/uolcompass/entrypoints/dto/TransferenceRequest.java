package br.com.uolcompass.entrypoints.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Schema(description = "Request payload to initiate a transference")
public record TransferenceRequest(

        @NotNull
        @Schema(description = "Payer wallet ID (sender)", example = "1")
        Long payerId,

        @NotNull
        @Schema(description = "Payee wallet ID (receiver)", example = "2")
        Long payeeId,

        @NotNull @Positive
        @Schema(description = "Amount to transfer", example = "150.00")
        BigDecimal amount

) {
}
