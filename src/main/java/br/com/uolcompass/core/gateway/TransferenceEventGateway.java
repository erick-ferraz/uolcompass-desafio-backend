package br.com.uolcompass.core.gateway;

import br.com.uolcompass.core.domain.TransferenceDomain;

public interface TransferenceEventGateway {

    void publishInitiated(TransferenceDomain transferenceDomain);

}
