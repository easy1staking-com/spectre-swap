package com.easy1staking.spectrum.swap.bot;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.address.AddressProvider;
import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.backend.blockfrost.common.Constants;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFTransactionService;
import com.bloxbean.cardano.client.backend.model.TxContentUtxo;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.plutus.spec.ConstrPlutusData;
import com.bloxbean.cardano.client.plutus.spec.serializers.ConstrDataJsonSerializer;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.util.List;


public class SpectrumAddressDeserializationTest {


    @Test
    public void pkhTest1() {
        Address address = new Address("addr1qxpn7l9w3etzmgwdc9d4nl0fuwe20t7qjj7mzgeq6jmwtkknp3czr2pgelsluk2k7l2qajulygdndvm78zk2h2yjscms4exr5y");
        address.getPaymentCredentialHash()
                .ifPresent(hash -> System.out.println(HexUtil.encodeHexString(hash)));
        address.getDelegationCredentialHash()
                .ifPresent(hash -> System.out.println(HexUtil.encodeHexString(hash)));
    }

    @Test
    public void testWallet() {
        Address address = new Address("addr1qyz4gqkfggu27yn4qhqgkqglluvssradl66r84vey98w63qydrgr4asf539ejum3rlhstrcxls97w4p6xc62gzx67gjss8hyu5");
        address.getPaymentCredentialHash()
                .ifPresent(hash -> System.out.println(HexUtil.encodeHexString(hash)));

    }

    @Test
    public void testPoolAddress() {
        Address address = new Address("addr1x94ec3t25egvhqy2n265xfhq882jxhkknurfe9ny4rl9k6dj764lvrxdayh2ux30fl0ktuh27csgmpevdu89jlxppvrst84slu");
        address.getPaymentCredentialHash()
                .ifPresent(hash -> System.out.println(HexUtil.encodeHexString(hash)));

    }


}
