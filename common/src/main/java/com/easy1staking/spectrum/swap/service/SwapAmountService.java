package com.easy1staking.spectrum.swap.service;

import com.easy1staking.spectrum.swap.model.SwapResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;

@Component
@Slf4j
public class SwapAmountService {

    public SwapResult calculateSwapAmounts(BigInteger base,
                                           boolean isBaseX,
                                           BigInteger reserveX,
                                           BigInteger reserveY,
                                           BigInteger feeNum,
                                           BigInteger feeDen,
                                           BigInteger exFeeNum,
                                           BigInteger exFeeDen) {

        if (isBaseX) {
            BigInteger num = reserveY.multiply(base).multiply(feeNum);
            BigInteger den = (reserveX.multiply(feeDen)).add(base.multiply(feeNum));
            BigInteger actualQuote = num.divide(den);
            BigInteger batcherFees = actualQuote.multiply(exFeeNum).divide(exFeeDen);
            return new SwapResult(actualQuote, batcherFees);
        } else {
            BigInteger num = reserveX.multiply(base).multiply(feeNum);
            BigInteger den = (reserveY.multiply(feeDen)).add(base.multiply(feeNum));
            BigInteger actualQuote = num.divide(den);
            BigInteger batcherFees = actualQuote.multiply(exFeeNum).divide(exFeeDen);
            return new SwapResult(actualQuote, batcherFees);
        }

    }
}
