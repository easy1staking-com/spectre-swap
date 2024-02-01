package com.easy1staking.spectrum.swap.model;


import com.bloxbean.cardano.client.transaction.spec.Transaction;

public record TxBytes(byte[] txBytes, Transaction transaction) {
}
