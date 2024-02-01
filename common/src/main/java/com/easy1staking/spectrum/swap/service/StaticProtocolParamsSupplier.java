package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.ProtocolParamsSupplier;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.backend.ogmios.OgmiosEpochService;
import io.adabox.client.OgmiosWSClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StaticProtocolParamsSupplier implements ProtocolParamsSupplier {

    private final OgmiosWSClient ogmiosWSClient;

    private ProtocolParams protocolParams;

    @Override
    public ProtocolParams getProtocolParams() {
        return protocolParams;
    }

    @PostConstruct
    public void preloadProtocolParams() {
        OgmiosEpochService ogmiosEpochService = new OgmiosEpochService(ogmiosWSClient);
        protocolParams = ogmiosEpochService.getProtocolParameters().getValue();
    }

}
