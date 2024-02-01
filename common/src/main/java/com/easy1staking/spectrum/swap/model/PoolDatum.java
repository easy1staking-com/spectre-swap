package com.easy1staking.spectrum.swap.model;

import java.math.BigInteger;

public record PoolDatum(AssetType x, AssetType y, AssetType nft, BigInteger feeNum, BigInteger minX) {
}
