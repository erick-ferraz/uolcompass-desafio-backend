package br.com.uolcompass.core.usecase.impl;

import org.springframework.http.HttpStatus;

public class SameWalletTransferException extends ApplicationBaseException {

    public SameWalletTransferException() {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "Cannot transfer to the same wallet");
    }

}
