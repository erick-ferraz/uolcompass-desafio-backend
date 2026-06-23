package br.com.uolcompass.core.gateway;

import br.com.uolcompass.core.domain.WalletDomain;

import java.util.Optional;

public interface WalletGateway {

    WalletDomain create(WalletDomain walletDomain);

    boolean existsByCpfCnpj(String cpfCnpj);

    Optional<WalletDomain> findById(Long id);

}