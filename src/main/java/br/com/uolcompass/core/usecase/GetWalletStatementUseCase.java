package br.com.uolcompass.core.usecase;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.domain.WalletDomain;

import java.util.List;

public interface GetWalletStatementUseCase {

    StatementResult execute(Long walletId, String rawPassword);

    record StatementResult(WalletDomain wallet, List<TransferenceDomain> transferences) {}
}
