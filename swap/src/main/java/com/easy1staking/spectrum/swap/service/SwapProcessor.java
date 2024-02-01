package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.yaci.core.common.NetworkType;
import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.core.model.TransactionInput;
import com.bloxbean.cardano.yaci.core.model.TransactionOutput;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalTxMonitorClient;
import com.bloxbean.cardano.yaci.helper.reactive.BlockStreamer;
import com.easy1staking.spectrum.swap.model.SwapDetails;
import com.easy1staking.spectrum.swap.util.AmountUtil;
import com.easy1staking.spectrum.swap.util.Timing;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adabox.client.OgmiosWSClient;
import io.adabox.model.query.response.ChainTip;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.easy1staking.spectrum.swap.model.Constants.SWAP_ADDRESSES;

@AllArgsConstructor
@Component
@Profile("!test")
@Slf4j
public class SwapProcessor {

    private final LocalClientProvider localClientProvider;

    private final SwapService swapService;

    private final NftPoolService nftPoolService;

    private final HybridUtxoSupplier hybridUtxoSupplier;

    private final OgmiosWSClient ogmiosWSClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void startProcessor() throws ApiException {

        log.info("Starting Spectrum Swap Processor");

        ArrayList<String> txAlreadySeen = new ArrayList<>(250);

        ChainTip chainTip = ogmiosWSClient.chainTip();

        long slot = chainTip.getPointOrOrigin().getSlot();
        String hash = chainTip.getPointOrOrigin().getHash();

        log.info("syncing from block, slot: {}, hash: {}", slot, hash);

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        BlockStreamer
//                .fromPoint("cardano-node-delta", 3000, new Point(slot, hash), NetworkType.MAINNET.getN2NVersionTable())
                .fromPoint(NetworkType.MAINNET, new Point(slot, hash))
                .stream()
                .subscribe(block -> {
                    nftPoolService.processBlock(block);
//                    this.processPendingTxInBlock(block);
                });

        try {
            Thread.sleep(5000L);
        } catch (InterruptedException e) {
            log.error("shoot", e);
        }

        /**
         * Couple of things sto check:
         * 1. calculations are correct. There seems to be rounding issue when calculating the amount for the user
         * 2. reference scripts
         */

        LocalTxMonitorClient localTxMonitorClient = localClientProvider.getTxMonitorClient();
        localTxMonitorClient
                .streamMempoolTransactions()
                .doOnError(foo -> log.error("ERROR!", foo))
                .flatMap(bytes -> {
                    try {
                        var txHash = TransactionUtil.getTxHash(bytes);
                        if (txAlreadySeen.contains(txHash)) {
                            if (txAlreadySeen.size() > 200) {
                                txAlreadySeen.clear();
                            }
                            return Flux.empty();
                        } else {
                            txAlreadySeen.add(txHash);

                            Transaction transaction = Transaction.deserialize(bytes);
                            Optional<Utxo> swapUtxoOpt = swapService.getSpectrumUtxo(transaction, txHash);
                            if (swapUtxoOpt.isPresent()) {
                                log.info("Swap UTXO: {}", swapUtxoOpt.get());
                                log.info("Swap Tx: {}", objectMapper.writeValueAsString(transaction));

                                var swapUtxo = swapUtxoOpt.get();

                                long time = System.currentTimeMillis();
                                Timing.TX_SEEN_TIME.put(txHash, time);

                                return Flux.just(new SwapDetails(txHash, transaction, swapUtxo));
                            } else {
                                nftPoolService.updateUtxo(transaction, txHash);
                                Timing.LAST_TX_SEEN_AT.set(System.currentTimeMillis());
                                return Flux.empty();
                            }

                        }
                    } catch (Exception e) {
                        return Flux.empty();
                    }
                })
                .subscribe(swapDetails -> {
                    var startTime = Timing.TX_SEEN_TIME.get(swapDetails.txHash());
                    log.info("elapsed {}ms - BEFORE addUtxoToMempool", System.currentTimeMillis() - startTime);
                    hybridUtxoSupplier.addUtxoToMempool(swapDetails.swapUtxo());
                    long ttl = swapDetails.transaction().getBody().getTtl();
                    try {
                        log.info("elapsed {}ms - BEFORE submitSwap", System.currentTimeMillis() - startTime);
                        swapService.executeSwap(swapDetails.swapUtxo(), ttl);
                    } catch (Exception e) {
                        log.warn("Thrown error", e);
                    }
                });

    }


    private void processPendingTxInBlock(Block block) {
        List<String> txHashesToSkip = block.getTransactionBodies().stream().flatMap(tx -> tx.getInputs().stream().map(TransactionInput::getTransactionId)).toList();

        block.getTransactionBodies()
                .stream()
                .filter(transactionBody -> !txHashesToSkip.contains(transactionBody.getTxHash()))
                .filter(transactionBody -> transactionBody.getOutputs().stream().anyMatch(output -> SWAP_ADDRESSES.contains(output.getAddress())))
                .forEach(transactionBody -> {

                    log.info("found pending tx: {}", transactionBody.getTxHash());

                    for (int i = 0; i < transactionBody.getOutputs().size(); i++) {

                        final int j = i;

                        TransactionOutput output = transactionBody
                                .getOutputs()
                                .get(i);

                        if (SWAP_ADDRESSES.contains(output.getAddress())) {
                            Utxo utxo = Utxo.builder()
                                    .txHash(transactionBody.getTxHash())
                                    .outputIndex(j)
                                    .address(output.getAddress())
                                    .amount(output.getAmounts().stream().map(AmountUtil::toAmountCore).toList())
                                    .dataHash(output.getDatumHash())
                                    .inlineDatum(output.getInlineDatum())
                                    .referenceScriptHash(output.getScriptRef())
                                    .build();
                            log.info("submitting pending tx: {}", utxo);
                            swapService.executeSwap(utxo, block.getHeader().getHeaderBody().getSlot() + 600L);
                        }
                    }

                });
    }

}
