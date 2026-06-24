package br.com.uolcompass.core.gateway;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;

import java.util.List;
import java.util.Optional;

public interface TransferenceGateway {

    TransferenceDomain create(TransferenceDomain transferenceDomain);

    Optional<TransferenceDomain> findById(Long id);

    void updateStatus(Long id, TransferenceStatus status);

    List<TransferenceDomain> findByWalletIdOrderByCreatedAtDesc(Long walletId);

}
