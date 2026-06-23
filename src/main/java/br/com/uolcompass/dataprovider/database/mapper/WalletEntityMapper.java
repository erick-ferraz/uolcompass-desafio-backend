package br.com.uolcompass.dataprovider.database.mapper;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.dataprovider.database.entity.WalletEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WalletEntityMapper {

    WalletEntity toEntity(WalletDomain domain);

    WalletDomain toDomain(WalletEntity entity);
}
