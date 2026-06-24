package br.com.uolcompass.entrypoints.mapper;

import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.entrypoints.dto.WalletCreationRequest;
import br.com.uolcompass.entrypoints.dto.WalletResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface WalletDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "balance", expression = "java(java.math.BigDecimal.ZERO)")
    WalletDomain toDomain(WalletCreationRequest request);

    WalletResponse toResponse(WalletDomain domain);
}
