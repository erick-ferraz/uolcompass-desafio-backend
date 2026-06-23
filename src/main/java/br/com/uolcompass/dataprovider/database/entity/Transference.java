package br.com.uolcompass.dataprovider.database.entity;

import jakarta.persistence.Entity;
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
public class Transference extends BaseEntity {

    private BigDecimal amount;
    @ManyToOne
    @JoinColumn(name = "payer_id")
    private Wallet payer;
    @ManyToOne
    @JoinColumn(name = "payee_id")
    private Wallet payee;
}
