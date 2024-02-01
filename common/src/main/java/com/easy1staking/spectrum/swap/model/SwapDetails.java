package com.easy1staking.spectrum.swap.model;


import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.transaction.spec.Transaction;

public record SwapDetails(String txHash, Transaction transaction, Utxo swapUtxo) {
}
