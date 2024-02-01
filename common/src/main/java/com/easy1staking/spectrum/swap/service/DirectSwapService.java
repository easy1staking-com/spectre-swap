package com.easy1staking.spectrum.swap.service;

import com.easy1staking.spectrum.swap.model.ArbitrageResults;
import com.easy1staking.spectrum.swap.model.AssetType;
import com.easy1staking.spectrum.swap.model.PoolDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Optional;

@Component
@Slf4j
public class DirectSwapService {

    private static final BigInteger FEE_DEN = BigInteger.valueOf(1000L);

    /**
     * Few assumptions here, X is always Ada
     * deltaX can be negative, this means we are purchasing Ada and selling the Y asset
     *
     * @param deltaX      amount of ada to buy/sell
     * @param poolDetails the details of the AMM pool
     * @return deltaY
     */
    public BigInteger calculateDeltaY(BigInteger deltaX, PoolDetails poolDetails) {
        var dxf = deltaX.multiply(poolDetails.feeNum());

        if (deltaX.compareTo(BigInteger.ZERO) > 0) {
            var dYNum = poolDetails.reserveY().multiply(dxf);
            var dYDen = poolDetails.reserveX().multiply(FEE_DEN).add(dxf);
            var dY = dYNum.divide(dYDen);
            return dY.multiply(BigInteger.valueOf(-1L));
        } else {
            var dYNum = deltaX.multiply(poolDetails.reserveY()).multiply(FEE_DEN);
            var dYDen = poolDetails.reserveX().multiply(poolDetails.feeNum()).add(dxf);
            return dYNum.divide(dYDen).multiply(BigInteger.valueOf(-1L));
        }

    }

    public BigInteger calculateDeltaX(BigInteger deltaY, PoolDetails poolDetails) {
        var dyf = deltaY.multiply(poolDetails.feeNum());

        if (deltaY.compareTo(BigInteger.ZERO) > 0) {
            var dXNum = poolDetails.reserveX().multiply(dyf);
            var dXDen = poolDetails.reserveY().multiply(FEE_DEN).add(dyf);
            return dXNum.divide(dXDen).multiply(BigInteger.valueOf(-1L));
        } else {
            var dXNum = deltaY.multiply(poolDetails.reserveX()).multiply(FEE_DEN);
            var dXDen = poolDetails.reserveY().multiply(poolDetails.feeNum()).add(dyf);
            return dXNum.divide(dXDen).multiply(BigInteger.valueOf(-1L));
        }

    }

}
