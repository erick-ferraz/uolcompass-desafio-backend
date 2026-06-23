package br.com.uolcompass.core.usecase.impl;

import org.springframework.http.HttpStatus;

public class InsufficientBalanceException extends ApplicationBaseException {

    public InsufficientBalanceException(Long walletId) {
        super(HttpStatus.BAD_REQUEST, "Insufficient balance for wallet: " + walletId);
    }

}
