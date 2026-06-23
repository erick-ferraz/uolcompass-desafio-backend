package br.com.uolcompass.dataprovider.database.gateway;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.dataprovider.database.entity.WalletEntity;
import br.com.uolcompass.dataprovider.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletGatewayImpl implements WalletGateway {

    private final WalletRepository walletRepository;

    @Override
    public WalletDomain create(WalletDomain walletDomain) {
        var entity = new WalletEntity();
        entity.setName(walletDomain.getName());
        entity.setCpfCnpj(walletDomain.getCpfCnpj());
        entity.setEmail(walletDomain.getEmail());
        entity.setPassword(walletDomain.getPassword());
        entity.setBalance(walletDomain.getBalance());
        entity.setType(walletDomain.getType());

        var saved = walletRepository.save(entity);

        return toDomain(saved);
    }

    @Override
    public boolean existsByCpfCnpj(String cpfCnpj) {
        return walletRepository.existsByCpfCnpj(cpfCnpj);
    }

    private WalletDomain toDomain(WalletEntity entity) {
        return new WalletDomain(
                entity.getId(),
                entity.getName(),
                entity.getCpfCnpj(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getBalance(),
                entity.getType()
        );
    }
}
