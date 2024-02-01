package com.easy1staking.spectrum.swap.model;


import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

/**
 * @param base           the asset type of the token to exchange from
 * @param quote          the asset type of the token to exchange to
 * @param poolNft        the nft guarding the amm pool
 * @param poolFeeNum     amm pool fee numerator
 * @param exFeeNum       execution fee numerator (used to compute batcher fees)
 * @param exFeeDen       execution fee denominator
 * @param baseAmount     amount of the base to exchange (useful if it's ada or if it's not ada and more token were sent)
 * @param minQuoteAmount minimum about of the quote token to send back, if we can't send more skip tx for now
 */
public record SwapRequestDatum(AssetType base, AssetType quote, AssetType poolNft, BigInteger poolFeeNum,
                               BigInteger exFeeNum, BigInteger exFeeDen, String paymentPublicKeyHash,
                               Optional<String> stakingPublicKeyHash,
                               BigInteger baseAmount, BigInteger minQuoteAmount) {

}
