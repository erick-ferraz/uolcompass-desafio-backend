package br.com.uolcompass.core.domain;

import br.com.uolcompass.core.enums.TransferenceStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
@AllArgsConstructor
public class TransferenceDomain {

    private final Long id;
    private final Long payerId;
    private final Long payeeId;
    private final BigDecimal amount;
    private final TransferenceStatus status;

}
