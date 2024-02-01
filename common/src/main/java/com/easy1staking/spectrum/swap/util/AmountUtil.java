package com.easy1staking.spectrum.swap.util;

import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.common.CardanoConstants;
import com.bloxbean.cardano.client.transaction.spec.Asset;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.Value;

import java.util.List;

public class AmountUtil {

    public static Value fromAmount(Amount amount) {
        if (amount.getUnit().equals(CardanoConstants.LOVELACE)) {
            return Value.builder().coin(amount.getQuantity()).build();
        } else {
            return Value.builder()
                    .multiAssets(List.of(
                            MultiAsset.builder()
                                    .policyId(amount.getUnit().substring(0, 56))
                                    .assets(List.of(Asset.builder().name("0x" + amount.getUnit().substring(56)).value(amount.getQuantity()).build()))
                                    .build()
                    ))
                    .build();
        }
    }

    public static Amount toAmountCore(com.bloxbean.cardano.yaci.core.model.Amount amount) {
        return Amount.builder()
                .unit(amount.getUnit().replaceAll("\\.", ""))
                .quantity(amount.getQuantity())
                .build();
    }


}
