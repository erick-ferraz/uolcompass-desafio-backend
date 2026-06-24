package br.com.uolcompass.core.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class NotificationDomain {

    private final Long id;
    private final Long transferenceId;
    private final Long walletId;
    private final String message;
    private final LocalDateTime sentAt;
    private final String status;

}
