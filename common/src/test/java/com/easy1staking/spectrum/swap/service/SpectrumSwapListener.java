package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

@Slf4j
public class SpectrumSwapListener {

    private ObjectMapper objectMapper = new ObjectMapper();


    @Test
    public void startPropagating() throws InterruptedException {

        var swapService = new SwapService(null, null, null, null);

        String cardanoNodeHost = "delta.relay.easy1staking.com";
        int cardanoNodePort = 30100;

        LocalClientProvider localClientProvider = new LocalClientProvider(cardanoNodeHost, cardanoNodePort, Constants.MAINNET_PROTOCOL_MAGIC);
        localClientProvider.start();

        List<String> alreadySeenTx = new Vector<>();

        List<String> requestSwapTx = new Vector<>();

        LocalTxMonitorClient localTxMonitorClient = localClientProvider.getTxMonitorClient();
        localTxMonitorClient
                .streamMempoolTransactions()
                .flatMap(bytes -> {
                    try {
                        return Flux.just(Tuple.of(TransactionUtil.getTxHash(bytes), Transaction.deserialize(bytes)));
                    } catch (CborDeserializationException e) {
                        return Flux.empty();
                    }
                })
                .filter(hashAndTx -> {
                    if (alreadySeenTx.size() % 1000 == 0) {
                        alreadySeenTx.clear();
                    }
                    if (alreadySeenTx.contains(hashAndTx._1)) {
                        return false;
                    } else {
                        alreadySeenTx.add(hashAndTx._1);
                        return true;
                    }
                })
                .subscribe(hashAndTx -> {

                    if (requestSwapTx.size() % 1000 == 0) {
                        requestSwapTx.clear();
                    }

                    Optional<Utxo> spectrumUtxo = swapService.getSpectrumUtxo(hashAndTx._2(), hashAndTx._1());


                    var isSwapRequest = spectrumUtxo.isPresent();

//                    boolean isSwapRequest = hashAndTx._2.getBody()
//                            .getOutputs()
//                            .stream()
//                            .map(TransactionOutput::getAddress)
//                            .anyMatch(address -> address.equalsIgnoreCase(SPECTRUM_SWAP_ADDRESS));

                    try {
                        if (isSwapRequest) {
                            log.info("spectrumUtxo: {}", spectrumUtxo.get());

                            requestSwapTx.add(hashAndTx._1);
                            log.info("writing swap request");
                            String swapReqTxJson = objectMapper.writeValueAsString(hashAndTx._2());
                            String filename = String.format("%s-swap-req.json", hashAndTx._1().substring(0, 5));
                            FileOutputStream fos = new FileOutputStream("src/test/resources/" + filename);
                            fos.write(swapReqTxJson.getBytes());
                            fos.flush();

                            try {
                                log.info("req-{}: {}", hashAndTx._1(), hashAndTx._2.serializeToHex());
                            } catch (CborSerializationException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    if (!isSwapRequest) {

                        List<String> txInputHashes = hashAndTx._2.getBody()
                                .getInputs()
                                .stream()
                                .map(TransactionInput::getTransactionId)
                                .toList();

                        Optional<String> swapReqTx = txInputHashes.stream().filter(requestSwapTx::contains).findFirst();

                        if (swapReqTx.isPresent()) {
                            String swapReqTxHash = swapReqTx.get();
                            log.info("Found swap tx: {}", hashAndTx._1());

                            try {
                                String swapTxJson = objectMapper.writeValueAsString(hashAndTx._2());
                                String filename = String.format("%s-%s-swap.json", swapReqTxHash.substring(0, 5), hashAndTx._1());
                                FileOutputStream fos = new FileOutputStream("src/test/resources/" + filename);
                                fos.write(swapTxJson.getBytes());
                                fos.flush();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    }

                });

        Thread.sleep(999999);
    }

}
