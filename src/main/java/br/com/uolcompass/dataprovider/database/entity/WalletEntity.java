package br.com.uolcompass.dataprovider.database.entity;

import br.com.uolcompass.core.enums.WalletType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_wallets")
@Getter
@Setter
public class WalletEntity extends BaseEntity {

    private String name;
    @Column(name = "cpf_cnpj")
    private String cpfCnpj;
    private String email;
    private BigDecimal balance = BigDecimal.ZERO;
    private String password;
    @Enumerated(EnumType.STRING)
    private WalletType type;
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
