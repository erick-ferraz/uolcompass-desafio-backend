package br.com.uolcompass.core.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Wallet type: INDIVIDUAL (CPF) or BUSINESS (CNPJ)")
public enum WalletType {
    INDIVIDUAL,
    BUSINESS
}
