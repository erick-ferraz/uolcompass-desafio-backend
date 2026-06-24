package br.com.uolcompass.core.usecase;

import br.com.uolcompass.core.domain.WalletDomain;

public interface CreateWalletUseCase {

    WalletDomain execute(WalletDomain walletDomain);
}
