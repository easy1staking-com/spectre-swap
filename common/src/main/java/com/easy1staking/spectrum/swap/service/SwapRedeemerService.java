package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.plutus.spec.BigIntPlutusData;
import com.bloxbean.cardano.client.plutus.spec.ConstrPlutusData;
import com.easy1staking.spectrum.swap.model.PoolRedeemers;
import com.easy1staking.spectrum.swap.model.SwapRedeemers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@Slf4j
public class SwapRedeemerService {


    public SwapRedeemers createRedeemers(Utxo poolUtxo, Utxo swapUtxo) {

        String poolUtxoHash = poolUtxo.getTxHash();

        String swapUtxoTxHash = swapUtxo.getTxHash();

        int swapUtxoInputIndex;
        int poolUtxoInputIndex;
        int diff = swapUtxoTxHash.compareTo(poolUtxoHash);

        if (diff < 0) {
            swapUtxoInputIndex = 0;
            poolUtxoInputIndex = 1;
        } else {
            swapUtxoInputIndex = 1;
            poolUtxoInputIndex = 0;
        }


        return new SwapRedeemers(ConstrPlutusData.of(0, BigIntPlutusData.of(2), BigIntPlutusData.of(poolUtxoInputIndex)),
                BigInteger.valueOf(poolUtxoInputIndex),
                ConstrPlutusData.of(0,
                        BigIntPlutusData.of(poolUtxoInputIndex),
                        BigIntPlutusData.of(swapUtxoInputIndex),
                        BigIntPlutusData.of(1),
                        BigIntPlutusData.of(0)),
                BigInteger.valueOf(swapUtxoInputIndex));
    }

    public PoolRedeemers createPoolRedeemers(Utxo poolA, Utxo poolB) {

        String poolAHash = poolA.getTxHash();
        String poolBhash = poolB.getTxHash();

        int poolAIndex;
        int poolBIndex;
        int diff = poolAHash.compareTo(poolBhash);

        if (diff < 0) {
            poolAIndex = 0;
            poolBIndex = 1;
        } else {
            poolAIndex = 1;
            poolBIndex = 0;
        }


        return new PoolRedeemers(ConstrPlutusData.of(0, BigIntPlutusData.of(2), BigIntPlutusData.of(poolAIndex)),
                BigInteger.valueOf(poolAIndex),
                ConstrPlutusData.of(0, BigIntPlutusData.of(2), BigIntPlutusData.of(poolBIndex)),
                BigInteger.valueOf(poolBIndex));
    }

}
