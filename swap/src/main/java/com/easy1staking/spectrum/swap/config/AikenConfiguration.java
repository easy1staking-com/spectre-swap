package com.easy1staking.spectrum.swap.config;

import com.bloxbean.cardano.aiken.AikenTransactionEvaluator;
import com.bloxbean.cardano.client.api.TransactionEvaluator;
import com.bloxbean.cardano.client.backend.api.DefaultProtocolParamsSupplier;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.easy1staking.spectrum.swap.service.HybridUtxoSupplier;
import com.easy1staking.spectrum.swap.service.NoOpTransactionEvaluator;
import com.easy1staking.spectrum.swap.service.ScriptProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AikenConfiguration {

    @Bean
    public TransactionEvaluator transactionEvaluator(HybridUtxoSupplier hybridUtxoSupplier,
                                                     BFBackendService bfBackendService,
                                                     ScriptProvider jpgstoreScriptProvider) {
        return new AikenTransactionEvaluator(hybridUtxoSupplier,
                new DefaultProtocolParamsSupplier(bfBackendService.getEpochService()),
                jpgstoreScriptProvider);
    }

    //    @Bean
    public TransactionEvaluator noOpTransactionEvaluator() {
        return new NoOpTransactionEvaluator();
    }

}
