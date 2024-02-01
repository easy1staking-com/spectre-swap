package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.easy1staking.spectrum.swap.SpringBaseTest;
import com.easy1staking.spectrum.swap.model.SwapResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Slf4j
class SwapServiceSimulationDataProviderTest extends SpringBaseTest {

    @Autowired
    private BFBackendService bfBackendService;

    @Autowired
    private SwapService swapService;


    @ParameterizedTest
    @CsvSource({
            "6b4c32db85f5b0b4ef1f545cf2dff5592b7df473cc6a1158e4413c3ea9851346,0,2c958e0da9c770ee18b192e11e638c546f4cf75af106f65bf065f88b94b8b3fb,0",
            "b0e42fc5f3a8c3f84582c05c4177f31c6953e9591879cdaa99723db9be7a4975,0,c8ea73f0e840c2ff6c67b8c6ca47f042b95556fc5ad82d18af9e002ba487401c,0",
            "ccd92e4031619e90754cd063b437c67f89328ceeda70817b25b68e6607f1df21,0,29ffeec801460b081425eb5b5aa99d255fc161ce4ec2e4f2e3feaceea68353a0,0",
            "db76ffce352e8cebcae72b654303de28e1793315f93e74cbf11d54394a23815b,0,cc722b1a3bec96cf194c0b1ca4cd471cf19e222d3601b1567c2bbdeadae78006,0",
            "e12d5ca9fcafc5c971b33e8182fbcef9dc920101ed056c0f711c7c198178c491,0,d9d66e3b31076658101c954bd634b2eb5bdcae63e00b916fd256ee5e6b7fbbee,0",
            "ebd1415e41e85bb930220d3bc656ee5599d91a2db9b914c1c7b019680e3fea7e,0,cff1c640c8d48182dd41b901ff5a8dd9c7b2b34c2f91394897e8e59dd7c61242,0",
            "ef5ee529fddd0527d4ce77c2ee1022a3166948a2b7a8a5308ce2f1bfca466d4a,0,f1cc1b6ea60e7000dff3a474cfc2d86cf82409a2b6da31ae373827fc5c2e1912,0",
    })
    public void parameterizedTest(String swapUtxoHash, int swapUtxoIdx, String poolUtxoHash, int poolUtxoIdx) throws Exception {

        Result<Utxo> poolUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput(poolUtxoHash, poolUtxoIdx);

        Result<Utxo> swapUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput(swapUtxoHash, swapUtxoIdx);

        log.info("poolUtxo: {}", poolUtxo.getValue());
        log.info("swapUtxo: {}", swapUtxo.getValue());

        Optional<SwapResult> swapResults = Optional.empty();
//        var swapResults = Optional.of(new SwapResult(476532378L, 1530000));

        swapService.executeSwap(swapUtxo.getValue(), 1000l, Optional.of(poolUtxo.getValue()), swapResults);

    }


}