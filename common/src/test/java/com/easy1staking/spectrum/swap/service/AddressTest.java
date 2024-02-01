package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.common.model.Networks;
import io.adabox.util.HexUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class AddressTest {

    @Test
    public void scriptAddress() {
        Credential script = Credential.fromScript("edfff663d37fc5f9753bc4222e0da2bfe08aa48db0837d2c329adeb3");
        Address entAddress = AddressProvider.getEntAddress(script, Networks.mainnet());
        System.out.println(entAddress.getAddress());
    }

    @Test
    public void pkh() {
        Address addr1 = new Address("addr1qxvxpe95p6nuq6grlsdfdazy3cdxzmksd7twuc4l9e5f6eavatgc4hdkune2k9xalx3tgskrva0g243ehggg8wkkpzdqse672e");
        String s = HexUtil.encodeHexString(addr1.getPaymentCredential().get().getBytes());
        System.out.println(s);
    }

    @Test
    public void paymentAddress() {
        Address addr1 = new Address("addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zr0vp2360e2j2gve54sxsheawjd6s6we2d25xl96a3r0jdqzvyqkl");
        addr1.getPaymentCredential().ifPresent(credential -> {
            Address entAddress = AddressProvider.getEntAddress(credential, Networks.mainnet());
            System.out.println(entAddress.getAddress());
        });
        System.out.println(AddressProvider.getStakeAddress(addr1).getAddress());
    }

    @Test
    public void newMnemonic() {
        var account = new Account();
        var mnemonic = account.mnemonic();
        log.info(mnemonic);
    }


}
