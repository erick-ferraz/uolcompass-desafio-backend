package br.com.uolcompass.entrypoints.mapper;

import br.com.uolcompass.core.domain.TransferenceDomain;
import br.com.uolcompass.core.domain.WalletDomain;
import br.com.uolcompass.core.gateway.WalletGateway;
import br.com.uolcompass.entrypoints.dto.StatementItem;
import br.com.uolcompass.entrypoints.dto.StatementResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StatementDtoMapper {

    private final WalletGateway walletGateway;

    public StatementResponse toResponse(
            WalletDomain wallet,
            List<TransferenceDomain> transferences) {

        var counterpartyIds = new ArrayList<Long>();
        for (var t : transferences) {
            counterpartyIds.add(
                t.getPayerId().equals(wallet.getId())
                    ? t.getPayeeId()
                    : t.getPayerId()
            );
        }

        var counterparties = walletGateway.findAllById(counterpartyIds);
        var nameById = counterparties.stream()
                .collect(Collectors.toMap(WalletDomain::getId, WalletDomain::getName));

        var items = new ArrayList<StatementItem>(transferences.size());
        for (var t : transferences) {
            var isSent = t.getPayerId().equals(wallet.getId());
            var counterpartyId = isSent ? t.getPayeeId() : t.getPayerId();
            items.add(new StatementItem(
                    t.getId(),
                    isSent ? "SENT" : "RECEIVED",
                    nameById.getOrDefault(counterpartyId, "Unknown"),
                    t.getAmount(),
                    null,
                    t.getStatus()
            ));
        }

        return new StatementResponse(
                wallet.getId(),
                wallet.getName(),
                wallet.getBalance(),
                items
        );
    }
}
