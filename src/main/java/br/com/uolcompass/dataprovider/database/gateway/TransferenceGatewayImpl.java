package br.com.uolcompass.dataprovider.database.gateway;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.enums.TransferenceStatus;
import br.com.uolcompass.core.gateway.TransferenceGateway;
import br.com.uolcompass.dataprovider.database.mapper.TransferenceEntityMapper;
import br.com.uolcompass.dataprovider.repository.TransferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransferenceGatewayImpl implements TransferenceGateway {

    private final TransferenceRepository transferenceRepository;
    private final TransferenceEntityMapper transferenceEntityMapper;

    @Override
    @Transactional
    public TransferenceDomain create(TransferenceDomain transferenceDomain) {
        var entity = transferenceEntityMapper.toEntity(transferenceDomain);
        var saved = transferenceRepository.save(entity);
        return transferenceEntityMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransferenceDomain> findById(Long id) {
        return transferenceRepository.findById(id)
                .map(transferenceEntityMapper::toDomain);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, TransferenceStatus status) {
        transferenceRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(status);
            transferenceRepository.save(entity);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferenceDomain> findByWalletIdOrderByCreatedAtDesc(Long walletId) {
        return transferenceRepository.findByWalletIdOrderByCreatedAtDesc(walletId)
                .stream()
                .map(transferenceEntityMapper::toDomain)
                .toList();
    }

}
