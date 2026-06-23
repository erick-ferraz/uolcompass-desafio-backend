package br.com.uolcompass.core.usecase.impl;

import org.springframework.http.HttpStatus;

public class WalletNotFoundException extends ApplicationBaseException {

    public WalletNotFoundException(Long walletId) {
        super(HttpStatus.NOT_FOUND, "Wallet not found: " + walletId);
    }

}
