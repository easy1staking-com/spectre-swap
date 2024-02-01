package com.easy1staking.spectrum.swap.model;

import com.bloxbean.cardano.client.plutus.spec.PlutusData;

import java.math.BigInteger;

public record SwapRedeemers(PlutusData poolRedeemer, BigInteger poolInputIndex, PlutusData swapRedeemer, BigInteger swapInputIndex) {
}
