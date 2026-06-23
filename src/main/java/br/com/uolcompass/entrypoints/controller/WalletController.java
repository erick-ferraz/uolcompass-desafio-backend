package br.com.uolcompass.entrypoints.controller;

import br.com.uolcompass.entrypoints.dto.WalletCreationRequest;
import br.com.uolcompass.entrypoints.dto.WalletResponse;
import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.usecase.CreateWalletUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@Tag(name = "Wallets", description = "Endpoints for wallet management")
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;

    @Operation(summary = "Create a new wallet")
    @ApiResponse(responseCode = "201", description = "Wallet created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    @ApiResponse(responseCode = "409", description = "CPF/CNPJ already registered", content = @Content)
    @PostMapping
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody final WalletCreationRequest request) {
        var response = createWalletUseCase.execute(request.toDomain());
        return ResponseEntity.status(HttpStatus.CREATED).body(WalletResponse.from(response));
    }
}
