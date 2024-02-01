package com.easy1staking.spectrum.swap.util;

import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Utxo;

public class UtxoUtil {

    public static Utxo toUtxo(io.adabox.model.query.response.models.Utxo utxo, String address) {
        return Utxo.builder()
                .txHash(utxo.getTxId())
                .outputIndex(utxo.getIndex().intValue())
                .address(address)
                .inlineDatum(utxo.getDatum())
                .amount(utxo.getAmountList().stream().map(amount -> Amount.builder()
                        .unit(amount.getUnit().replaceAll("\\.", ""))
                        .quantity(amount.getQuantity())
                        .build()).toList())
                .build();
    }

}
