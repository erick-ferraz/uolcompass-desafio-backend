package br.com.uolcompass.core.domain;

import br.com.uolcompass.core.enums.WalletType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
public class WalletDomain {

    private final Long id;
    private final String name;
    private final String cpfCnpj;
    private final String email;
    private final String password;
    private final BigDecimal balance;
    private final WalletType type;
    private final Long version;
    private final LocalDateTime createdAt;

}
