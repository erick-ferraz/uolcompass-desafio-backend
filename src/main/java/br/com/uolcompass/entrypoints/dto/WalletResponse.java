package br.com.uolcompass.entrypoints.dto;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.enums.WalletType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response payload for wallet operations")
public record WalletResponse(
        @Schema(description = "Wallet unique identifier")
        Long id,

        @Schema(description = "Full name")
        String name,

        @Schema(description = "CPF or CNPJ")
        String cpfCnpj,

        @Schema(description = "Email address")
        String email,

        @Schema(description = "Current balance")
        BigDecimal balance,

        @Schema(description = "Wallet type")
        WalletType type
) {
    public static WalletResponse from(WalletDomain walletDomain) {
        return new WalletResponse(
                walletDomain.getId(),
                walletDomain.getName(),
                walletDomain.getCpfCnpj(),
                walletDomain.getEmail(),
                walletDomain.getBalance(),
                walletDomain.getType()
        );
    }
}
