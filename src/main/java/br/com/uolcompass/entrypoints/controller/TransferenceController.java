package br.com.uolcompass.entrypoints.controller;

import br.com.uolcompass.core.usecase.GetTransferenceStatusUseCase;
import br.com.uolcompass.core.usecase.InitiateTransferenceUseCase;
import br.com.uolcompass.entrypoints.dto.TransferenceRequest;
import br.com.uolcompass.entrypoints.dto.TransferenceResponse;
import br.com.uolcompass.entrypoints.mapper.TransferenceDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ProblemDetail;

@Tag(name = "Transferences", description = "Endpoints for transference management")
@RestController
@RequestMapping("/api/v1/transferences")
@RequiredArgsConstructor
public class TransferenceController {

    private final InitiateTransferenceUseCase initiateTransferenceUseCase;
    private final GetTransferenceStatusUseCase getTransferenceStatusUseCase;
    private final TransferenceDtoMapper transferenceDtoMapper;

    @Operation(summary = "Initiate a new transference")
    @ApiResponse(responseCode = "202", description = "Transference initiated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or business rule violation",
                 content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Payer or payee wallet not found",
                 content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "422", description = "Cannot transfer to the same wallet",
                 content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<TransferenceResponse> initiate(
            @Valid @RequestBody final TransferenceRequest request) {
        var domain = transferenceDtoMapper.toDomain(request);
        var result = initiateTransferenceUseCase.execute(domain);
        var response = transferenceDtoMapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @Operation(summary = "Get transference status by ID")
    @ApiResponse(responseCode = "200", description = "Transference found")
    @ApiResponse(responseCode = "404", description = "Transference not found",
                 content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{id}")
    public ResponseEntity<TransferenceResponse> getStatus(@PathVariable final Long id) {
        var result = getTransferenceStatusUseCase.execute(id);
        return result
                .map(transferenceDtoMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}
