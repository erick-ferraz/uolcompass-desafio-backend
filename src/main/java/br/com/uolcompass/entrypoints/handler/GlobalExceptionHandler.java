package br.com.uolcompass.entrypoints.handler;

import br.com.uolcompass.core.usecase.impl.CpfCnpjAlreadyExistsException;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CpfCnpjAlreadyExistsException.class)
    public ProblemDetail handleCpfCnpjAlreadyExists(CpfCnpjAlreadyExistsException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problem.setTitle("CPF/CNPJ already registered");
        problem.setDetail(ex.getMessage());
        problem.setInstance(URI.create("/api/v1/wallets"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        var problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation error");
        problem.setDetail(ex.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce((a, b) -> a + "; " + b)
                .orElse("Invalid request"));
        problem.setInstance(URI.create("/api/v1/wallets"));
        return problem;
    }
}
