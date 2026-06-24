package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.core.usecase.CreateWalletUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreateWalletUseCaseImpl implements CreateWalletUseCase {

    private final WalletGateway walletGateway;
    private final PasswordEncoder passwordEncoder;

    @Override
    public WalletDomain execute(WalletDomain walletDomain) {
        if (walletGateway.existsByCpfCnpj(walletDomain.getCpfCnpj())) {
            throw new CpfCnpjAlreadyExistsException(walletDomain.getCpfCnpj());
        }

        var hashedPassword = passwordEncoder.encode(walletDomain.getPassword());
        var walletToCreate = new WalletDomain(
                null,
                walletDomain.getName(),
                walletDomain.getCpfCnpj(),
                walletDomain.getEmail(),
                hashedPassword,
                walletDomain.getBalance(),
                walletDomain.getType(),
                null,
                null
        );

        return walletGateway.create(walletToCreate);
    }
}
