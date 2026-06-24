package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.core.usecase.GetWalletStatementUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetWalletStatementUseCaseImpl implements GetWalletStatementUseCase {

    private final WalletGateway walletGateway;
    private final TransferenceGateway transferenceGateway;
    private final PasswordEncoder passwordEncoder;

    @Override
    public StatementResult execute(Long walletId, String rawPassword) {
        var wallet = walletGateway.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));

        if (!passwordEncoder.matches(rawPassword, wallet.getPassword())) {
            throw new WalletPasswordMismatchException();
        }

        var transferences = transferenceGateway.findByWalletIdOrderByCreatedAtDesc(walletId);

        return new StatementResult(wallet, transferences);
    }
}
