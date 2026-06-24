package br.com.uolcompass.entrypoints.dto;

import br.com.uolcompass.core.enums.TransferenceStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload for transference operations")
public record TransferenceResponse(

        @Schema(description = "Transference unique identifier")
        Long id,

        @Schema(description = "Current status of the transference")
        TransferenceStatus status,

        @Schema(description = "Informative message")
        String message

) {
}
