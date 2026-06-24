package br.com.uolcompass.entrypoints.controller;

import br.com.uolcompass.core.usecase.CreateWalletUseCase;
import br.com.uolcompass.core.usecase.GetWalletStatementUseCase;
import br.com.uolcompass.entrypoints.dto.StatementResponse;
import br.com.uolcompass.entrypoints.dto.WalletCreationRequest;
import br.com.uolcompass.entrypoints.dto.WalletResponse;
import br.com.uolcompass.entrypoints.mapper.StatementDtoMapper;
import br.com.uolcompass.entrypoints.mapper.WalletDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ProblemDetail;

@Tag(name = "Wallets", description = "Endpoints for wallet management")
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final GetWalletStatementUseCase getWalletStatementUseCase;
    private final WalletDtoMapper walletDtoMapper;
    private final StatementDtoMapper statementDtoMapper;

    @Operation(summary = "Create a new wallet")
    @ApiResponse(responseCode = "201", description = "Wallet created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data",
                 content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "CPF/CNPJ already registered",
                 content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody final WalletCreationRequest request) {
        var domain = walletDtoMapper.toDomain(request);
        var response = createWalletUseCase.execute(domain);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletDtoMapper.toResponse(response));
    }

    @Operation(summary = "Get wallet statement (password required)")
    @ApiResponse(responseCode = "200", description = "Statement retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Invalid wallet password",
                 content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Wallet not found",
                 content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    @Parameters(
        @Parameter(name = "X-Wallet-Password",
                   description = "Wallet password for authentication",
                   required = true,
                   in = ParameterIn.HEADER,
                   example = "securePass123")
    )
    @GetMapping("/{id}/statement")
    public ResponseEntity<StatementResponse> getStatement(
            @PathVariable final Long id,
            @RequestHeader("X-Wallet-Password") final String password) {
        var result = getWalletStatementUseCase.execute(id, password);
        var response = statementDtoMapper.toResponse(result.wallet(), result.transferences());
        return ResponseEntity.ok(response);
    }
}
