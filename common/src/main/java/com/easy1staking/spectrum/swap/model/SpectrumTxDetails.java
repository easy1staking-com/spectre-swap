package com.easy1staking.spectrum.swap.model;

import com.bloxbean.cardano.client.api.model.Utxo;

public record SpectrumTxDetails(SwapDetails swapDetails, Utxo swapRequestUtxo) {
}
