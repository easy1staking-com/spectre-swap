package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.api.util.ValueUtil;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.transaction.spec.MultiAsset;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import com.bloxbean.cardano.client.util.HexUtil;
import com.easy1staking.spectrum.swap.model.AssetType;
import com.easy1staking.spectrum.swap.model.SwapRequestDatum;
import com.easy1staking.spectrum.swap.model.SwapResult;
import com.easy1staking.spectrum.swap.util.Timing;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static com.easy1staking.spectrum.swap.model.Constants.SWAP_ADDRESSES;

@Component
@AllArgsConstructor
@Slf4j
public class SwapService {

    private final NftPoolService nftPoolService;

    private final SwapTransactionSubmitter swapTransactionSubmitter;

    private final DataParser dataParser;

    private final SwapAmountService swapAmountService;

    public boolean isSpectrumSwapRequest(Transaction transaction) {
        return getSwapTxOutput(transaction).isPresent();
    }

    public Optional<TransactionOutput> getSwapTxOutput(Transaction transaction) {
        return transaction.getBody()
                .getOutputs()
                .stream()
                .filter(txOutput -> SWAP_ADDRESSES.contains(txOutput.getAddress()))
                .findAny();
    }

    public Optional<Utxo> getSpectrumUtxo(Transaction transaction, String txHash) {
        List<TransactionOutput> transactionOutputs = transaction.getBody().getOutputs();

        Optional<Utxo> utxo = Optional.empty();
        for (int i = 0; i < transactionOutputs.size(); i++) {
            TransactionOutput transactionOutput = transactionOutputs.get(i);
            if (SWAP_ADDRESSES.contains(transactionOutput.getAddress())) {
                utxo = Optional.of(Utxo.builder()
                        .txHash(txHash)
                        .outputIndex(i)
                        .address(transactionOutput.getAddress())
                        .dataHash(HexUtil.encodeHexString(transactionOutput.getDatumHash()))
                        .inlineDatum(transactionOutput.getInlineDatum().serializeToHex())
                        .amount(ValueUtil.toAmountList(transactionOutput.getValue()))
                        .build());
            }
        }

        return utxo;
    }


    public void executeSwap(Utxo actualSwapUtxo, long ttl) {
        this.executeSwap(actualSwapUtxo, ttl, Optional.empty(), Optional.empty());
    }

    public void executeSwap(Utxo actualSwapUtxo, long ttl, Optional<Utxo> fixedPoolUtxo, Optional<SwapResult> fixedSwapResult) {

        try {
            var startTimeOpt = Optional.ofNullable(Timing.TX_SEEN_TIME.get(actualSwapUtxo.getTxHash()));
            startTimeOpt.ifPresent(startTime -> log.info("elapsed {}ms - executeSwap", System.currentTimeMillis() - startTime));

            PlutusData swapDatum = PlutusData.deserialize(HexUtil.decodeHexString(actualSwapUtxo.getInlineDatum()));
            Optional<String> datum = dataParser.deserializeDatum(swapDatum);

            if (datum.isPresent()) {
                String actualDatum = datum.get();

                SwapRequestDatum swapRequestDatum = dataParser.parseSwapRequestDatum(actualDatum);

                AssetType nftPool = swapRequestDatum.poolNft();

                Optional<Utxo> poolUtxo;
                if (fixedPoolUtxo.isPresent()) {
                    log.warn("Using fixedPoolUtxo");
                    poolUtxo = fixedPoolUtxo;
                } else {
                    poolUtxo = nftPoolService.getPoolUtxo(nftPool);
                }

                if (poolUtxo.isPresent()) {

                    Utxo actualPoolUtxo = poolUtxo.get();
                    Value value = actualPoolUtxo.toValue();

                    AssetType tokenAssetType;
                    if (swapRequestDatum.base().isAda()) {
                        tokenAssetType = swapRequestDatum.quote();
                    } else {
                        tokenAssetType = swapRequestDatum.base();
                    }

                    BigInteger reserveLovelaces = value.getCoin();
                    Optional<MultiAsset> reserveToken = value.getMultiAssets().stream().filter(multiAsset -> multiAsset.getPolicyId().equals(tokenAssetType.policyId())).findAny();

                    if (reserveToken.isPresent()) {
                        var actualReserveToken = reserveToken.get().getAssets().get(0).getValue();

                        SwapResult swapResult;
                        if (fixedSwapResult.isPresent()) {
                            log.warn("Using fixedSwapResult");
                            swapResult = fixedSwapResult.get();
                        } else {
                            swapResult = swapAmountService.calculateSwapAmounts(swapRequestDatum.baseAmount(),
                                    swapRequestDatum.base().isAda(),
                                    reserveLovelaces,
                                    actualReserveToken,
                                    swapRequestDatum.poolFeeNum(),
                                    BigInteger.valueOf(1000L),
                                    swapRequestDatum.exFeeNum(),
                                    swapRequestDatum.exFeeDen()
                            );
                        }

                        if (swapResult.quote().compareTo(swapRequestDatum.minQuoteAmount()) > 0) {
                            startTimeOpt.ifPresent(startTime -> log.info("elapsed {}ms - BEFORE buildSwapTransaction", System.currentTimeMillis() - startTime));
                            swapTransactionSubmitter.buildSwapTransaction(actualPoolUtxo, actualSwapUtxo, swapRequestDatum, swapResult, ttl);
                        } else {
                            log.info("Quote < MinQuote");
                        }


                    } else {

                        log.warn("Could not find token reserve for {} in swapUtxo {}", tokenAssetType, actualPoolUtxo);

                    }


                } else {
                    log.warn("could not find expected swapUtxo for nft {}", nftPool);
                }


            } else {
                log.warn("datum could not be found for tx {}", actualSwapUtxo.getTxHash());
            }


        } catch (Exception e) {
            log.warn(String.format("Error while processing tx %s", actualSwapUtxo.getTxHash()), e);
        }


    }

}
