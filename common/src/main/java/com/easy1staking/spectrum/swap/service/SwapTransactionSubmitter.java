package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.common.model.Networks;
import com.easy1staking.spectrum.swap.model.SwapRequestDatum;
import com.easy1staking.spectrum.swap.model.SwapResult;

public abstract class SwapTransactionSubmitter {

    public Address extractAddress(SwapRequestDatum swapRequestDatum) {
        return AddressProvider.getBaseAddress(Credential.fromKey(swapRequestDatum.paymentPublicKeyHash()),
                Credential.fromKey(swapRequestDatum.stakingPublicKeyHash().get()),
                Networks.mainnet());
    }

    abstract void buildSwapTransaction(Utxo poolUtxo,
                                       Utxo swapRequestUtxo,
                                       SwapRequestDatum swapRequestDatum,
                                       SwapResult swapResult,
                                       long ttl);
}
