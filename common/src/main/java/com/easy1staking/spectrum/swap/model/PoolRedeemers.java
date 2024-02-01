package com.easy1staking.spectrum.swap.model;

import com.bloxbean.cardano.client.plutus.spec.PlutusData;

import java.math.BigInteger;

public record PoolRedeemers(PlutusData poolARedeemer,
                            BigInteger poolAIndex,
                            PlutusData poolBRedeemer,
                            BigInteger poolBIndex) {
}
