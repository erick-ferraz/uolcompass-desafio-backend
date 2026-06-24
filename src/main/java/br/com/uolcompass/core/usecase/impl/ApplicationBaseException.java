package br.com.uolcompass.core.usecase.impl;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApplicationBaseException extends RuntimeException {

    private final HttpStatus status;

    protected ApplicationBaseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

}
