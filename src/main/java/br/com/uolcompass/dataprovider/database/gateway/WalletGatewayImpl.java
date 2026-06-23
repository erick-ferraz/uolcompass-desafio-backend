package br.com.uolcompass.dataprovider.database.gateway;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.dataprovider.database.mapper.WalletEntityMapper;
import br.com.uolcompass.dataprovider.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletGatewayImpl implements WalletGateway {

    private final WalletRepository walletRepository;
    private final WalletEntityMapper walletEntityMapper;

    @Override
    public WalletDomain create(WalletDomain walletDomain) {
        var entity = walletEntityMapper.toEntity(walletDomain);
        var saved = walletRepository.save(entity);
        return walletEntityMapper.toDomain(saved);
    }

    @Override
    public boolean existsByCpfCnpj(String cpfCnpj) {
        return walletRepository.existsByCpfCnpj(cpfCnpj);
    }
}
