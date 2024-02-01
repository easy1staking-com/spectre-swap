package com.easy1staking.spectrum.swap.bot;


import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.util.HexUtil;
import com.easy1staking.spectrum.swap.SpringBaseTest;
import com.easy1staking.spectrum.swap.model.Constants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PoolUtxoQueryTest extends SpringBaseTest {

    @Autowired
    private BFBackendService bfBackendService;

    @Test
    public void foo() throws ApiException {

        Map<Amount, Utxo> poolMap = new HashMap<>();

        int page = 1;
        int count = 10;
        Result<List<Utxo>> utxosResult = bfBackendService.getUtxoService().getUtxos(Constants.SPECTRUM_POOL_V1_ADDRESS, count, page++);
        while (utxosResult.isSuccessful() && !utxosResult.getValue().isEmpty()) {
            utxosResult.getValue().forEach(utxo -> {
                utxo.getAmount().stream()
                        .filter(amount -> !amount.getUnit().equals("lovelace"))
                        .filter(amount -> {
                            String assetName = new String(HexUtil.decodeHexString(amount.getUnit().substring(56)));
                            return amount.getQuantity().equals(BigInteger.ONE) && assetName.toLowerCase().endsWith("_nft");
                        })
                        .forEach(amount -> poolMap.put(amount, utxo));
            });
            utxosResult = bfBackendService.getUtxoService().getUtxos(Constants.SPECTRUM_POOL_V1_ADDRESS, count, page++);
        }

        poolMap.forEach((amount, utxo) -> {
            String policyId = amount.getUnit().substring(0, 56);
            String assetName = new String(HexUtil.decodeHexString(amount.getUnit().substring(56)));
            log.info("{}.{}, {}:{}", policyId, assetName, utxo.getTxHash(), utxo.getOutputIndex());
        });

    }
}
