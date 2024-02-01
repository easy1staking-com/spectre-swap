package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.address.Address;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.api.util.ValueUtil;
import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.function.helper.SignerProviders;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.client.quicktx.ScriptTx;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionInput;
import com.bloxbean.cardano.client.transaction.spec.Value;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.easy1staking.spectrum.swap.util.AmountUtil;
import com.easy1staking.spectrum.swap.model.SwapRedeemers;
import com.easy1staking.spectrum.swap.model.SwapRequestDatum;
import com.easy1staking.spectrum.swap.model.SwapResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@Primary
@Slf4j
public class QuickSwapTransactionSubmitter extends SwapTransactionSubmitter {

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
    private QuickTxBuilder quickTxBuilder;

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

        log.info("poolUtxo: {}", poolUtxo);
        log.info("swapRequestUtxo: {}", swapRequestUtxo);

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

            Value customerAmount = swapRequestUtxo.toValue()
                    .minus(AmountUtil.fromAmount(baseAmount))
                    .minus(AmountUtil.fromAmount(batcherFee))
                    .plus(AmountUtil.fromAmount(quoteAmount));

            SwapRedeemers redeemers = swapRedeemerService.createRedeemers(poolUtxo, swapRequestUtxo);

            PlutusData poolDatum = PlutusData.deserialize(HexUtil.decodeHexString(poolUtxo.getInlineDatum()));

            String poolHash, swapHash;
            int poolIndex = 0, swapIndex;
            if (nftPoolService.isTeddy(swapRequestDatum.poolNft().toUnit())) {
                poolHash = "cdafc4e33524e767c4d0ffde094d56fa42105dcfc9b62857974f86fd0e443c32";
                swapHash = "fb6906c2bc39777086036f9c46c297e9d8a41ede154b398d85245a2549b4bf04";
                swapIndex = 0;
            } else {
                if (nftPoolService.isV1(swapRequestDatum.poolNft().toUnit())) {
                    poolHash = "31a497ef6b0033e66862546aa2928a1987f8db3b8f93c59febbe0f47b14a83c6";
                    swapHash = "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a";
                    swapIndex = 2;
                } else {
                    poolHash = "c8c93656e8bce07fabe2f42d703060b7c71bfa2e48a2956820d1bd81cc936faa";
                    swapHash = "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a";
                    swapIndex = 2;
                }
            }


            ScriptTx scriptTx = new ScriptTx()
                    .collectFrom(poolUtxo, redeemers.poolRedeemer())
                    .collectFrom(swapRequestUtxo, redeemers.swapRedeemer())

                    // Amount to send to customer
                    .payToContract(poolUtxo.getAddress(), ValueUtil.toAmountList(newPoolValue), poolDatum)
                    .payToAddress(address.getAddress(), ValueUtil.toAmountList(customerAmount)) // this should be second always (i.e. index 1)
                    .payToAddress(account.baseAddress(), batcherFee)

                    .readFrom(poolHash, poolIndex)
                    .readFrom(swapHash, swapIndex)
                    .withChangeAddress(account.baseAddress());

            QuickTxBuilder.TxContext txContext = quickTxBuilder.compose(scriptTx)
                    .withSigner(SignerProviders.signerFrom(account))
                    .feePayer(account.baseAddress())
                    .withCollateralInputs(TransactionInput.builder()
                            .transactionId(accountService.getCollateralHash())
                            .index(accountService.getCollateralIndex())
                            .build())
                    .validTo(ttl)
//                    .collateralPayer(account.baseAddress())
                    .ignoreScriptCostEvaluationError(true);


//            txContext.completeAndWait();

            Transaction transaction = txContext.buildAndSign();
            ObjectMapper om = new ObjectMapper();
            log.info("Tx: {}", om.writeValueAsString(transaction));


        } catch (Exception e) {
            log.warn("error", e);
        }

    }


}
