package br.com.uolcompass.entrypoints.handler;

import br.com.uolcompass.core.usecase.impl.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void shouldHandleWalletNotFoundException() {
        var ex = new WalletNotFoundException(99L);

        var problem = handler.handleApplicationBase(ex);

        assertThat(problem.getStatus()).isEqualTo(404);
        assertThat(problem.getDetail()).contains("99");
    }

    @Test
    void shouldHandleInsufficientBalanceException() {
        var ex = new InsufficientBalanceException(1L);

        var problem = handler.handleApplicationBase(ex);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).contains("Insufficient balance");
    }

    @Test
    void shouldHandleBusinessWalletCannotTransferException() {
        var ex = new BusinessWalletCannotTransferException();

        var problem = handler.handleApplicationBase(ex);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getDetail()).contains("Business wallets");
    }

    @Test
    void shouldHandleSameWalletTransferException() {
        var ex = new SameWalletTransferException();

        var problem = handler.handleApplicationBase(ex);

        assertThat(problem.getStatus()).isEqualTo(422);
    }

    @Test
    void shouldHandleCpfCnpjAlreadyExistsException() {
        var ex = new CpfCnpjAlreadyExistsException("123");

        var problem = handler.handleApplicationBase(ex);

        assertThat(problem.getStatus()).isEqualTo(409);
    }

    @Test
    void shouldHandleWalletPasswordMismatchException() {
        var ex = new WalletPasswordMismatchException();

        var problem = handler.handleApplicationBase(ex);

        assertThat(problem.getStatus()).isEqualTo(401);
        assertThat(problem.getDetail()).contains("Invalid wallet password");
    }

    @Test
    void shouldHandleValidationErrors() {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "test");
        bindingResult.reject("error", "Field is required");
        var ex = new MethodArgumentNotValidException(null, bindingResult);

        var problem = handler.handleValidationErrors(ex);

        assertThat(problem.getStatus()).isEqualTo(400);
        assertThat(problem.getTitle()).isEqualTo("Validation error");
    }
}
