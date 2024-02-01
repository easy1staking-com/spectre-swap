package com.easy1staking.spectrum.swap.bot.util;

import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.transaction.spec.Value;
import com.easy1staking.spectrum.swap.util.AmountUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

class AmountUtilTest {


    @Test
    public void test1() {
        long lovelaces = 123000000L;
        Value value = AmountUtil.fromAmount(Amount.lovelace(BigInteger.valueOf(lovelaces)));
        Assertions.assertEquals(value.getMultiAssets().size(), 0);
        Assertions.assertEquals(value.getCoin().longValue(), lovelaces);
    }

    @Test
    public void test2() {
        Amount asset = Amount.asset("a5bb0e5bb275a573d744a021f9b3bff73595468e002755b447e01559", "0x484f534b594361736847726162303030303533373532", BigInteger.valueOf(123000000L));
        Value value = AmountUtil.fromAmount(asset);
        Assertions.assertEquals(value.getCoin().longValue(), 0L);
        Assertions.assertEquals(value.getMultiAssets().size(), 1);
        Assertions.assertEquals(value.getMultiAssets().get(0).getPolicyId(), "a5bb0e5bb275a573d744a021f9b3bff73595468e002755b447e01559");
        Assertions.assertEquals(value.getMultiAssets().get(0)
                .getAssets().size(), 1);
        Assertions.assertEquals(value.getMultiAssets().get(0)
                .getAssets().get(0).getName(), "0x484f534b594361736847726162303030303533373532");
        Assertions.assertEquals(value.getMultiAssets().get(0)
                .getAssets().get(0).getValue().longValue(), 123000000L);
    }


}