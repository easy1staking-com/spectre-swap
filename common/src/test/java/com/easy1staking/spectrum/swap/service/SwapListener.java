package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.easy1staking.spectrum.swap.model.TxBytes;
import com.easy1staking.spectrum.swap.model.SwapDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Vector;

@Slf4j
public class SwapListener {

    private final SwapService swapService = new SwapService(null, null, null, null);

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Test
    public void startListening() throws InterruptedException {

        String cardanoNodeHost = "delta.relay.easy1staking.com";
        int cardanoNodePort = 30100;

        LocalClientProvider localClientProvider = new LocalClientProvider(cardanoNodeHost, cardanoNodePort, Constants.MAINNET_PROTOCOL_MAGIC);
        localClientProvider.start();

        List<String> txAlreadySeen = new Vector<>();

        LocalTxMonitorClient localTxMonitorClient = localClientProvider.getTxMonitorClient();
        localTxMonitorClient
                .streamMempoolTransactions()
                .flatMap(bytes -> {
                    try {
                        Transaction transaction = Transaction.deserialize(bytes);
                        if (swapService.getSwapTxOutput(transaction).isPresent()) {
                            return Flux.just(new TxBytes(bytes, transaction));
                        } else {
                            return Flux.empty();
                        }
                    } catch (CborDeserializationException e) {
                        return Flux.empty();
                    }
                })
                .flatMap(transactionBytes -> {
                    try {
                        String txHash = TransactionUtil.getTxHash(transactionBytes.transaction());

                        if (txAlreadySeen.contains(txHash)) {
                            return Flux.empty();
                        } else {
                            if (txAlreadySeen.size() > 100) {
                                txAlreadySeen.clear();
                            }
                            txAlreadySeen.add(txHash);

                            return Flux.just(new SwapDetails(txHash, transactionBytes.transaction(), null));
                        }

                    } catch (Exception e) {
                        return Flux.empty();
                    }
                })

                .subscribe(hashAndTx -> {
                    log.info("txHash: {}", hashAndTx.txHash());
                    try {
                        log.info("    Tx: {}", objectMapper.writeValueAsString(hashAndTx.transaction()));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });

        Thread.sleep(999999);
    }

}
