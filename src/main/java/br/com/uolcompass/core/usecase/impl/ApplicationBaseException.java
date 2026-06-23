package br.com.uolcompass.core.usecase.impl;

import org.springframework.http.HttpStatus;

public abstract class ApplicationBaseException extends RuntimeException {

    private final HttpStatus status;

    protected ApplicationBaseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

}
