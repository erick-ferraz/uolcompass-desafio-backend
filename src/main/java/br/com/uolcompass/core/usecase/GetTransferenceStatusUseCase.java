package br.com.uolcompass.core.usecase;

import br.com.uolcompass.core.domain.TransferenceDomain;

import java.util.Optional;

public interface GetTransferenceStatusUseCase {

    Optional<TransferenceDomain> execute(Long id);

}
