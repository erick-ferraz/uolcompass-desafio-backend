package br.com.uolcompass.dataprovider.messaging.dto;

import java.math.BigDecimal;

public record TransferenceDebitedEvent(
        Long transferenceId,
        Long payerId,
        Long payeeId,
        BigDecimal amount
) {}
