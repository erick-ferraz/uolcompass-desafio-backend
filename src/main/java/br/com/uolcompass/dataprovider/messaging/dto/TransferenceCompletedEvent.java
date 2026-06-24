package br.com.uolcompass.dataprovider.messaging.dto;

import java.math.BigDecimal;

public record TransferenceCompletedEvent(
        Long transferenceId,
        Long payerId,
        Long payeeId,
        BigDecimal amount
) {}
