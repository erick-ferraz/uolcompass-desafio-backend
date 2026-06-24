package br.com.uolcompass.core.usecase.impl;

import org.springframework.http.HttpStatus;

public class WalletPasswordMismatchException extends ApplicationBaseException {

    public WalletPasswordMismatchException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid wallet password");
    }

}
