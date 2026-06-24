package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.core.usecase.CreateWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateWalletUseCaseImpl implements CreateWalletUseCase {

    private final WalletGateway walletGateway;

    @Override
    public WalletDomain execute(WalletDomain walletDomain) {
        if (walletGateway.existsByCpfCnpj(walletDomain.getCpfCnpj())) {
            throw new CpfCnpjAlreadyExistsException(walletDomain.getCpfCnpj());
        }
        return walletGateway.create(walletDomain);
    }
}
