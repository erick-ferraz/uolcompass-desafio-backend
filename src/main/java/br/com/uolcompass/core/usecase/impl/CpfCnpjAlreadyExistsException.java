package br.com.uolcompass.core.usecase.impl;

public class CpfCnpjAlreadyExistsException extends RuntimeException {

    public CpfCnpjAlreadyExistsException(String cpfCnpj) {
        super("CPF/CNPJ already registered: " + cpfCnpj);
    }
}
