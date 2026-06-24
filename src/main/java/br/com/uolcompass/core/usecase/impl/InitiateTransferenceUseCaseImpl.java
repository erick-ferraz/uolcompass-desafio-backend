package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.enums.WalletType;
import br.com.uolcompass.core.gateway.TransferenceEventGateway;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.core.usecase.InitiateTransferenceUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InitiateTransferenceUseCaseImpl implements InitiateTransferenceUseCase {

    private final WalletGateway walletGateway;
    private final TransferenceGateway transferenceGateway;
    private final TransferenceEventGateway transferenceEventGateway;

    @Override
    public TransferenceDomain execute(TransferenceDomain transferenceDomain) {
        var payerId = transferenceDomain.getPayerId();
        var payeeId = transferenceDomain.getPayeeId();
        var amount = transferenceDomain.getAmount();

        var payer = walletGateway.findById(payerId)
                .orElseThrow(() -> new WalletNotFoundException(payerId));

        if (payer.getType() == WalletType.BUSINESS) {
            throw new BusinessWalletCannotTransferException();
        }

        walletGateway.findById(payeeId)
                .orElseThrow(() -> new WalletNotFoundException(payeeId));

        if (payerId.equals(payeeId)) {
            throw new SameWalletTransferException();
        }

        if (payer.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(payerId);
        }

        var domainToCreate = new TransferenceDomain(
                null, payerId, payeeId, amount, TransferenceStatus.PENDING
        );
        var saved = transferenceGateway.create(domainToCreate);

        transferenceEventGateway.publishInitiated(saved);

        return saved;
    }

}
