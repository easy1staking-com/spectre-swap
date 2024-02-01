package com.easy1staking.spectrum.swap.model;

import java.math.BigInteger;

public record PoolDetails(AssetType xType,
                          BigInteger reserveX,
                          AssetType yType,
                          BigInteger reserveY,
                          BigInteger minReserveX,
                          BigInteger feeNum,
                          AssetType nft,
                          PoolType poolType) {

    public BigInteger getK() {
        return reserveX.multiply(reserveY);
    }

    public BigInteger getFeeDen() {
        return BigInteger.valueOf(1000L);
    }

    public boolean isValidPool() {
        return reserveX.compareTo(minReserveX) > 0;
    }

    public double price() {
        return reserveX.doubleValue() / reserveY.doubleValue();
    }

}
