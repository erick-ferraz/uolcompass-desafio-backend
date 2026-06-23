package br.com.uolcompass.core.gateway;

import br.com.uolcompass.core.domain.WalletDomain;

public interface WalletGateway {

    WalletDomain create(WalletDomain walletDomain);

    boolean existsByCpfCnpj(String cpfCnpj);
}
