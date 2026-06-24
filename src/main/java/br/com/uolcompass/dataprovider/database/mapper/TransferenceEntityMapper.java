package br.com.uolcompass.dataprovider.database.mapper;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.dataprovider.database.entity.TransferenceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = WalletEntityResolver.class)
public interface TransferenceEntityMapper {

    @Mapping(target = "payer", source = "payerId")
    @Mapping(target = "payee", source = "payeeId")
    TransferenceEntity toEntity(TransferenceDomain domain);

    @Mapping(target = "payerId", source = "payer")
    @Mapping(target = "payeeId", source = "payee")
    TransferenceDomain toDomain(TransferenceEntity entity);

}
