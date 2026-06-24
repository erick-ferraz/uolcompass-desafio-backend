package br.com.uolcompass.entrypoints.mapper;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.entrypoints.dto.TransferenceRequest;
import br.com.uolcompass.entrypoints.dto.TransferenceResponse;
import br.com.uolcompass.core.enums.TransferenceStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferenceDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(br.com.uolcompass.core.enums.TransferenceStatus.PENDING)")
    TransferenceDomain toDomain(TransferenceRequest request);

    @Mapping(target = "message", expression = "java(buildMessage(domain.getStatus(), domain.getId()))")
    TransferenceResponse toResponse(TransferenceDomain domain);

    default String buildMessage(TransferenceStatus status, Long id) {
        return switch (status) {
            case PENDING -> "Transference initiated. Track status at /api/v1/transferences/" + id;
            case DEBITED -> "Amount debited from payer. Track status at /api/v1/transferences/" + id;
            case COMPLETED -> "Transference completed successfully.";
            case FAILED -> "Transference failed.";
            case COMPENSATED -> "Transference compensated.";
        };
    }

}
