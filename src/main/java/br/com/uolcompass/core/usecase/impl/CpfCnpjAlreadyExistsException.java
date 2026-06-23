package br.com.uolcompass.core.usecase.impl;

import org.springframework.http.HttpStatus;

public class CpfCnpjAlreadyExistsException extends ApplicationBaseException {

    public CpfCnpjAlreadyExistsException(String cpfCnpj) {
        super(HttpStatus.CONFLICT, "CPF/CNPJ already registered: " + cpfCnpj);
    }

}