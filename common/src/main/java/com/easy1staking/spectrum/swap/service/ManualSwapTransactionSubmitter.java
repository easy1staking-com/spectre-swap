package com.easy1staking.spectrum.swap.service;

import co.nstant.in.cbor.CborException;
import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.ProtocolParams;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.exception.CborRuntimeException;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.plutus.spec.*;
import com.bloxbean.cardano.client.plutus.util.ScriptDataHashGenerator;
import com.bloxbean.cardano.client.transaction.spec.*;
import com.bloxbean.cardano.client.transaction.util.CostModelUtil;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.easy1staking.spectrum.swap.client.SubmitApiClient;
import com.easy1staking.spectrum.swap.model.PoolType;
import com.easy1staking.spectrum.swap.model.SwapRedeemers;
import com.easy1staking.spectrum.swap.model.SwapRequestDatum;
import com.easy1staking.spectrum.swap.model.SwapResult;
import com.easy1staking.spectrum.swap.util.AmountUtil;
import com.easy1staking.spectrum.swap.util.Timing;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.bloxbean.cardano.client.plutus.spec.RedeemerTag.Spend;

@Component
@Slf4j
public class ManualSwapTransactionSubmitter extends SwapTransactionSubmitter {

    @org.springframework.beans.factory.annotation.Value("${swap.transaction.submit}")
    private boolean submitTransaction;

    @org.springframework.beans.factory.annotation.Value("${swap.transaction.submitTo}")
    private String submitTo;

    @Autowired
    private AccountService accountService;

    @Autowired
    private Account account;
    @Autowired
    private NftPoolService nftPoolService;
    @Autowired
    private SwapRedeemerService swapRedeemerService;
    @Autowired
    private TransactionService ogmiosTransactionService;
    @Autowired
    private StaticProtocolParamsSupplier staticProtocolParamsSupplier;
    @Autowired
    private LocalTxSubmissionClient localTxSubmissionClient;

    @Autowired
    private SubmitApiClient submitApiClient;
    @Autowired
    private FastTxSigner fastTxSigner;

    private final ObjectMapper om = new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("INIT submitTransaction: {}", submitTransaction);
        log.info("INIT submitTo: {}", submitTo);
    }

    public void buildSwapTransaction(Utxo poolUtxo,
                                     Utxo swapRequestUtxo,
                                     SwapRequestDatum swapRequestDatum,
                                     SwapResult swapResult,
                                     long ttl) {

//        log.info("poolUtxo: {}", poolUtxo);
//        log.info("swapRequestUtxo: {}", swapRequestUtxo);

        try {

            Address address = extractAddress(swapRequestDatum);

            Amount baseAmount;
            if (swapRequestDatum.base().isAda()) {
                baseAmount = Amount.lovelace(swapRequestDatum.baseAmount());
            } else {
                baseAmount = Amount.asset(swapRequestDatum.base().toUnit(), swapRequestDatum.baseAmount());
            }

            Amount quoteAmount;
            if (swapRequestDatum.quote().isAda()) {
                quoteAmount = Amount.lovelace(swapResult.quote());
            } else {
                quoteAmount = Amount.asset(swapRequestDatum.quote().toUnit(), swapResult.quote());
            }

            Value value = poolUtxo.toValue();
            Value newPoolValue = value.plus(AmountUtil.fromAmount(baseAmount)).minus(AmountUtil.fromAmount(quoteAmount));

            Amount batcherFee = Amount.lovelace(swapResult.batcherFee());

            SwapRedeemers redeemers = swapRedeemerService.createRedeemers(poolUtxo, swapRequestUtxo);

            PlutusData poolDatum = PlutusData.deserialize(HexUtil.decodeHexString(poolUtxo.getInlineDatum()));

            Optional<PoolType> poolTypeOpt = nftPoolService.getPoolType(swapRequestDatum.poolNft().toUnit());

            if (poolTypeOpt.isPresent()) {
                String poolHash, swapHash;
                int poolIndex = 0, swapIndex;

                switch (poolTypeOpt.get()) {
                    case SPECTRUM_V1:
                        poolHash = "31a497ef6b0033e66862546aa2928a1987f8db3b8f93c59febbe0f47b14a83c6";
                        swapHash = "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a";
                        swapIndex = 2;
                        break;
                    case SPECTRUM_V2:
                        poolHash = "c8c93656e8bce07fabe2f42d703060b7c71bfa2e48a2956820d1bd81cc936faa";
                        swapHash = "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a";
                        swapIndex = 2;
                        break;
                    case TEDDY:
                        poolHash = "cdafc4e33524e767c4d0ffde094d56fa42105dcfc9b62857974f86fd0e443c32";
                        swapHash = "fb6906c2bc39777086036f9c46c297e9d8a41ede154b398d85245a2549b4bf04";
                        swapIndex = 0;
                        break;
                    default:
                        throw new Exception("Could not determine pool");
                }

                /**
                 * FeePayer issue, in this case I might not want to specify it.
                 * Fees Should be deducted from teh swapRequestUtxo
                 */
                try {

                    var manualFee = BigInteger.valueOf(291337L);

                    Value manualCustomerAmount = swapRequestUtxo.toValue()
                            .minus(AmountUtil.fromAmount(baseAmount))
                            .minus(AmountUtil.fromAmount(batcherFee))
//                        .minus(Value.builder().coin(manualFee).build())
                            .plus(AmountUtil.fromAmount(quoteAmount));

                    var manualBatcherFee = AmountUtil.fromAmount(batcherFee).minus(Value.builder().coin(manualFee).build());

                    var transaction = builtTxManually(poolUtxo,
                            swapRequestUtxo,
                            List.of(
                                    TransactionOutput.builder()
                                            .address(poolUtxo.getAddress())
                                            .value(newPoolValue)
                                            .inlineDatum(poolDatum)
                                            .build(),
                                    TransactionOutput.builder()
                                            .address(address.getAddress())
                                            .value(manualCustomerAmount)
                                            .build(),
                                    TransactionOutput.builder()
                                            .address(account.baseAddress())
                                            .value(manualBatcherFee)
                                            .build()
                            ),
                            List.of(
                                    TransactionInput.builder().transactionId(poolHash).index(poolIndex).build(),
                                    TransactionInput.builder()
                                            .transactionId(swapHash)
                                            .index(swapIndex).build()
                            ),
                            List.of(Redeemer.builder()
                                            .tag(Spend)
                                            .data(redeemers.poolRedeemer())
                                            .index(redeemers.poolInputIndex())
                                            .exUnits(ExUnits.builder()
                                                    .mem(BigInteger.valueOf(530000L))
                                                    .steps(BigInteger.valueOf(165000000L))
                                                    .build())
                                            .build(),
                                    Redeemer.builder()
                                            .tag(Spend)
                                            .data(redeemers.swapRedeemer())
                                            .index(redeemers.swapInputIndex())
                                            .exUnits(ExUnits.builder()
                                                    .mem(BigInteger.valueOf(270000L))
                                                    .steps(BigInteger.valueOf(140000000L))
                                                    .build())
                                            .build()
                            ),
                            TransactionInput.builder()
                                    .transactionId(accountService.getCollateralHash())
                                    .index(accountService.getCollateralIndex())
                                    .build(),
                            manualFee,
                            ttl);

                    var startTimeOpt = Optional.ofNullable(Timing.TX_SEEN_TIME.get(swapRequestUtxo.getTxHash()));
                    startTimeOpt.ifPresent(startTime -> log.info("elapsed {}ms - BEFORE unsafeSign", System.currentTimeMillis() - startTime));

                    byte[] txBytes = fastTxSigner.sign(transaction);
                    startTimeOpt.ifPresent(startTime -> log.info("elapsed {}ms - AFTER unsafeSign", System.currentTimeMillis() - startTime));

                    if (submitTransaction) {

                        if (submitTo.equals("submitApi")) {

                            startTimeOpt.ifPresent(startTime -> log.info("ready to submit in {}", (System.currentTimeMillis() - startTime)));
                            submitApiClient.submit(txBytes);
                            log.info("submitted {}, tx: {}", TransactionUtil.getTxHash(txBytes), om.writeValueAsString(transaction));

                        } else if (submitTo.equals("local")) {

//                            long startTime = Timing.TX_SEEN_TIME.get(swapRequestUtxo.getTxHash());
                            startTimeOpt.ifPresent(startTime -> log.info("ready to submit in {}", (System.currentTimeMillis() - startTime)));
                            localTxSubmissionClient.submitTxCallback(new TxSubmissionRequest(txBytes));
                            log.info("submitted {}, tx: {}", TransactionUtil.getTxHash(txBytes), om.writeValueAsString(transaction));

                        } else if (submitTo.equals("ogmios")) {
//                            long startTime = Timing.TX_SEEN_TIME.get(swapRequestUtxo.getTxHash());
                            startTimeOpt.ifPresent(startTime -> log.info("ready to submit in {}", (System.currentTimeMillis() - startTime)));
                            Result<String> stringResult = ogmiosTransactionService.submitTransaction(txBytes);
                            startTimeOpt.ifPresent(startTime -> log.info("tx submitted in {}", (System.currentTimeMillis() - startTime)));

                            log.info("Swap Tx: {} ", om.writeValueAsString(transaction));
                            String txHash = TransactionUtil.getTxHash(transaction);

                            if (stringResult.isSuccessful()) {
                                log.info("successfully submitted tx {}", txHash);
                            } else {
                                log.warn("error while submitting tx {}, response: {}, value: {}", txHash, stringResult.getResponse(), stringResult.getValue());

                            }

                        }

                    }


                } catch (Exception e) {
                    log.warn("Error", e);
                }

            } else {
                log.warn("PoolType for {} not found", swapRequestDatum.poolNft().toUnit());
            }

//            if (nftPoolService.isTeddy(swapRequestDatum.poolNft().toUnit())) {
//                poolHash = "cdafc4e33524e767c4d0ffde094d56fa42105dcfc9b62857974f86fd0e443c32";
//                swapHash = "fb6906c2bc39777086036f9c46c297e9d8a41ede154b398d85245a2549b4bf04";
//                swapIndex = 0;
//            } else {
//                if (nftPoolService.isV1(swapRequestDatum.poolNft().toUnit())) {
//                    poolHash = "31a497ef6b0033e66862546aa2928a1987f8db3b8f93c59febbe0f47b14a83c6";
//                    swapHash = "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a";
//                    swapIndex = 2;
//                } else {
//                    poolHash = "c8c93656e8bce07fabe2f42d703060b7c71bfa2e48a2956820d1bd81cc936faa";
//                    swapHash = "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a";
//                    swapIndex = 2;
//                }
//            }


        } catch (Exception e) {
            log.warn(String.format("Could not buildSwapTransaction"), e);
        }
    }


    private Transaction builtTxManually(Utxo poolUtxo,
                                        Utxo swapUtxo,
                                        List<TransactionOutput> outputs,
                                        List<TransactionInput> referenceScripts,
                                        List<Redeemer> redeemers,
                                        TransactionInput collateral,
                                        BigInteger fee,
                                        long ttl) throws CborException, CborSerializationException {

        var sortedRedeemers = redeemers.stream().sorted(Comparator.comparing(Redeemer::getIndex)).toList();

        List<TransactionInput> inputs = new ArrayList<>();
        inputs.add(TransactionInput.builder().transactionId(poolUtxo.getTxHash()).index(poolUtxo.getOutputIndex()).build());
        inputs.add(TransactionInput.builder().transactionId(swapUtxo.getTxHash()).index(swapUtxo.getOutputIndex()).build());

        inputs.sort(Comparator.comparing(TransactionInput::getTransactionId));

        var transaction = Transaction.builder()
                .body(TransactionBody.builder().inputs(inputs)
                        .outputs(outputs)
                        .referenceInputs(referenceScripts)
                        .collateral(List.of(collateral))
                        .fee(fee)
                        .ttl(ttl)
                        .build())
                .witnessSet(TransactionWitnessSet.builder()
                        .redeemers(sortedRedeemers)
                        .build())
                .build();

        calculateScriptDataHash(transaction, staticProtocolParamsSupplier.getProtocolParams());

//        CostMdls costMdls = new CostMdls();
//
//        Optional<CostModel> costModelFromProtocolParams = CostModelUtil.getCostModelFromProtocolParams(staticProtocolParamsSupplier.getProtocolParams(), Language.PLUTUS_V2);
//        costMdls.add(costModelFromProtocolParams.orElse(PlutusV2CostModel));
//
////        long startTime = System.currentTimeMillis();
//        byte[] scriptDataHash = ScriptDataHashGenerator.generate(transaction.getWitnessSet().getRedeemers(),
//                transaction.getWitnessSet().getPlutusDataList(), costMdls.getLanguageViewEncoding());
//
////        log.info("data hash: {}ms", (System.currentTimeMillis() - startTime));
//
//        transaction.getBody().setScriptDataHash(scriptDataHash);

        return transaction;
    }

    public static void calculateScriptDataHash(Transaction transaction, ProtocolParams protocolParams) {
        boolean containsPlutusScript = false;
        if (transaction.getWitnessSet().getPlutusV1Scripts() != null && transaction.getWitnessSet().getPlutusV1Scripts().size() > 0 || transaction.getWitnessSet().getPlutusV2Scripts() != null && transaction.getWitnessSet().getPlutusV2Scripts().size() > 0 || transaction.getWitnessSet().getRedeemers() != null && transaction.getWitnessSet().getRedeemers().size() > 0) {
            containsPlutusScript = true;
        }

        CostMdls costMdls = null;
        if (costMdls == null) {
            costMdls = new CostMdls();
            Optional costModel;
            if (transaction.getWitnessSet().getPlutusV1Scripts() != null && transaction.getWitnessSet().getPlutusV1Scripts().size() > 0) {
                costModel = com.bloxbean.cardano.client.transaction.util.CostModelUtil.getCostModelFromProtocolParams(protocolParams, Language.PLUTUS_V1);
                costMdls.add((CostModel) costModel.orElse(com.bloxbean.cardano.client.transaction.util.CostModelUtil.PlutusV1CostModel));
            }

            if (transaction.getWitnessSet().getPlutusV2Scripts() != null && transaction.getWitnessSet().getPlutusV2Scripts().size() > 0) {
                costModel = com.bloxbean.cardano.client.transaction.util.CostModelUtil.getCostModelFromProtocolParams(protocolParams, Language.PLUTUS_V2);
                costMdls.add((CostModel) costModel.orElse(com.bloxbean.cardano.client.transaction.util.CostModelUtil.PlutusV2CostModel));
            }

            if (costMdls.isEmpty() && transaction.getBody().getReferenceInputs() != null && transaction.getBody().getReferenceInputs().size() > 0) {
                costModel = com.bloxbean.cardano.client.transaction.util.CostModelUtil.getCostModelFromProtocolParams(protocolParams, Language.PLUTUS_V2);
                costMdls.add((CostModel) costModel.orElse(CostModelUtil.PlutusV2CostModel));
            }
        }

        if (containsPlutusScript) {
            byte[] scriptDataHash;
            try {
                scriptDataHash = ScriptDataHashGenerator.generate(transaction.getWitnessSet().getRedeemers(), transaction.getWitnessSet().getPlutusDataList(), costMdls.getLanguageViewEncoding());
            } catch (CborException | CborSerializationException var6) {
                throw new CborRuntimeException(var6);
            }

            transaction.getBody().setScriptDataHash(scriptDataHash);
        }

    }

}
