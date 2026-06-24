package br.com.uolcompass.core.gateway;

import br.com.uolcompass.core.domain.WalletDomain;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface WalletGateway {

    WalletDomain create(WalletDomain walletDomain);

    boolean existsByCpfCnpj(String cpfCnpj);

    Optional<WalletDomain> findById(Long id);

    void updateBalance(Long walletId, BigDecimal newBalance);

    List<WalletDomain> findAllById(Collection<Long> ids);

    List<WalletDomain> findAll();

}