package br.com.uolcompass.core.usecase.impl;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.core.usecase.GetTransferenceStatusUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetTransferenceStatusUseCaseImpl implements GetTransferenceStatusUseCase {

    private final TransferenceGateway transferenceGateway;

    @Override
    public Optional<TransferenceDomain> execute(Long id) {
        return transferenceGateway.findById(id);
    }

}
