package br.com.uolcompass.core.usecase.impl;

import org.springframework.http.HttpStatus;

public class BusinessWalletCannotTransferException extends ApplicationBaseException {

    public BusinessWalletCannotTransferException() {
        super(HttpStatus.BAD_REQUEST, "Business wallets cannot initiate transfers");
    }

}
