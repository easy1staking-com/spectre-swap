package com.easy1staking.spectrum.swap.bot;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.backend.blockfrost.common.Constants;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFTransactionService;
import com.bloxbean.cardano.client.backend.model.TxContentUtxo;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.plutus.spec.ConstrPlutusData;
import com.bloxbean.cardano.client.plutus.spec.serializers.ConstrDataJsonSerializer;
import com.bloxbean.cardano.client.util.HexUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.jupiter.api.Test;

import java.util.List;


public class SpectrumInspectTx {


    @Test
    public void basic() throws ApiException {

        // Req b4efcadb559365f4544a82a8729fe8319e360762230b71ff1554dbd0c5c6b283
        // Swap 67cd122fe2348f3f58617e4a8ad924655e0d73ac2039971693a6de0fa2d2e354

        String swapRequestTxHash = "b4efcadb559365f4544a82a8729fe8319e360762230b71ff1554dbd0c5c6b283";
        String swapTxHash = "67cd122fe2348f3f58617e4a8ad924655e0d73ac2039971693a6de0fa2d2e354";

//        TransactionService transactionService = new BFTransactionService("https://blockfrost-api.mainnet.dandelion.blockwarelabs.io", "whatever");
        TransactionService transactionService = new BFTransactionService(Constants.BLOCKFROST_MAINNET_URL, "mainnetKWaNkQcrF1erC3u3SZjaFxZiM2M20jFM");

        Result<TxContentUtxo> transactionUtxos = transactionService.getTransactionUtxos(swapRequestTxHash);
        transactionUtxos
                .getValue()
                .getOutputs()
                .stream()
                .filter(txOut -> txOut.getAddress().equalsIgnoreCase("addr1wynp362vmvr8jtc946d3a3utqgclfdl5y9d3kn849e359hsskr20n"))
                .forEach(System.out::println);

    }

    @Test
    public void deserializeSwapRequestDatum() throws CborException {
        ObjectMapper objectMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(ConstrPlutusData.class, new ConstrDataJsonSerializer());
        objectMapper.registerModule(module);

        String datum = "d8799fd8799f581c95a427e384527065f2f8946f5e86320d0117839a5e98ea2c0b55fb004448554e54ffd8799f4040ffd8799f581ce08fbaa73db55294b3b31f2a365be5c4b38211a47880f0ef6b17a1604c48554e545f4144415f4e4654ff1903e51b00018f18f54f76661b0de0b6b3a7640000581c055402c94238af127505c08b011fff19080fadfeb433d599214eed44d8799f581c0468d03af609a44b9973711fef058f06fc0be7543a3634a408daf225ff1b00000002e6735fa41acbbf63b5ff";

        List<DataItem> decode = CborDecoder.decode(HexUtil.decodeHexString(datum));
        System.out.println(decode.size());
        decode.forEach(dataItem -> {
            try {
                ConstrPlutusData data = ConstrPlutusData.deserialize(dataItem);

                System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data));
            } catch (CborDeserializationException | JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });

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
