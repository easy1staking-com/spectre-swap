package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.model.Utxo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@Slf4j
@AllArgsConstructor
public class ParallelSwapService {

    private final SwapService swapService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public void submitSwap(Utxo actualSwapUtxo, long ttl) {
        executorService.submit(() -> swapService.executeSwap(actualSwapUtxo, ttl));
    }

}
