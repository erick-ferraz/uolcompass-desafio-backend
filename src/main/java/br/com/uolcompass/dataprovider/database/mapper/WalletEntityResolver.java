package br.com.uolcompass.dataprovider.database.mapper;

import br.com.uolcompass.dataprovider.database.entity.WalletEntity;
import br.com.uolcompass.dataprovider.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletEntityResolver {

    private final WalletRepository walletRepository;

    public WalletEntity fromId(Long id) {
        if (id == null) return null;
        return walletRepository.getReferenceById(id);
    }

    public Long toId(WalletEntity entity) {
        if (entity == null) return null;
        return entity.getId();
    }

}
