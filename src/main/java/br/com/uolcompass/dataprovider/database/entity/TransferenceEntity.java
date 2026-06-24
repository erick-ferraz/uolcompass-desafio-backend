package br.com.uolcompass.dataprovider.database.entity;

import br.com.uolcompass.core.enums.TransferenceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_transferences")
@NoArgsConstructor
@Getter
@Setter
public class TransferenceEntity extends BaseEntity {

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "payer_id", nullable = false)
    private WalletEntity payer;

    @ManyToOne
    @JoinColumn(name = "payee_id", nullable = false)
    private WalletEntity payee;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransferenceStatus status;

}
