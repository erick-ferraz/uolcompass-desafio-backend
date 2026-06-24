package br.com.uolcompass.dataprovider.messaging.dto;

import br.com.uolcompass.core.enums.TransferenceStatus;

import java.math.BigDecimal;

public record TransferenceFailedEvent(
        Long transferenceId,
        Long payerId,
        Long payeeId,
        BigDecimal amount,
        TransferenceStatus currentStatus,
        String reason
) {}
