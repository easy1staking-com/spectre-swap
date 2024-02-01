package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.TransactionEvaluator;
import com.bloxbean.cardano.client.api.TransactionProcessor;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.EvaluationResult;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.bloxbean.cardano.yaci.helper.model.TxResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class LocalTransactionProcessor implements TransactionProcessor {

    private final LocalTxSubmissionClient localTxSubmissionClient;

    private final TransactionEvaluator transactionEvaluator;

    @Override
    public Result<String> submitTransaction(byte[] cborData) throws ApiException {
        log.info("About to submit tx");
        TxResult result = localTxSubmissionClient.submitTx(new TxSubmissionRequest(cborData)).block();
        if (result.isAccepted()) {
            log.info("TX accepted");
            return Result.success(result.getTxHash());
        } else {
            log.info("TX not accepted");
            return Result.error(result.getErrorCbor());
        }
    }

    @Override
    public Result<List<EvaluationResult>> evaluateTx(byte[] cbor, Set<Utxo> inputUtxos) throws ApiException {
        log.info("Running {} tx evaluation", transactionEvaluator.getClass());
        long startTime = System.currentTimeMillis();
        Result<List<EvaluationResult>> listResult = transactionEvaluator.evaluateTx(cbor, inputUtxos);
        log.info("Tx evaluation ran in {}ms", System.currentTimeMillis() - startTime);
        return listResult;
    }

    @PostConstruct
    public void logInfo() {
        log.info("running evaluator: {}", transactionEvaluator.getClass());
    }

}
