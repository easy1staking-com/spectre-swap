package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.api.UtxoSupplier;
import com.bloxbean.cardano.client.api.common.OrderEnum;
import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.api.util.ValueUtil;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.transaction.util.TransactionUtil;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.model.Block;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Vector;

@Component
@RequiredArgsConstructor
@Slf4j
public class HybridUtxoSupplier implements UtxoSupplier {

    private final BFBackendService backendService;

    private final NftPoolService nftPoolService;

    private final Account account;
    private final List<Utxo> mempoolTransactions = new ArrayList<>(250);

    private final List<Utxo> walletUtxo = new Vector<>();

    private final List<Utxo> scriptUtxos = new ArrayList<>();

    @Override
    public List<Utxo> getPage(String address, Integer nrOfItems, Integer page, OrderEnum order) {
        return walletUtxo;
    }

    @Override
    public Optional<Utxo> getTxOutput(String txHash, int outputIndex) {
        return findScriptUtxo(txHash, outputIndex)
                .or(() -> findPoolUtxo(txHash, outputIndex))
                .or(() -> findWalletUtxo(txHash, outputIndex))
                .or(() -> findMempoolUtxo(txHash, outputIndex))
                .or(() -> findUtxoFromBlockfrost(txHash, outputIndex));
    }

    private Optional<Utxo> findScriptUtxo(String txHash, int outputIndex) {
        return scriptUtxos.stream().filter(utxo -> utxo.getTxHash().equals(txHash) && utxo.getOutputIndex() == outputIndex)
                .findAny();
    }

    private Optional<Utxo> findMempoolUtxo(String txHash, int outputIndex) {
        Optional<Utxo> mempoolUtxo = mempoolTransactions
                .stream()
                .filter(utxo -> utxo.getTxHash().equals(txHash) && utxo.getOutputIndex() == outputIndex).findAny();
        return mempoolUtxo;
    }

    private Optional<Utxo> findWalletUtxo(String txHash, int outputIndex) {

        Optional<Utxo> walletUtxo = this.walletUtxo
                .stream()
                .filter(utxo -> utxo.getTxHash().equals(txHash) && utxo.getOutputIndex() == outputIndex)
                .findFirst();

        return walletUtxo;
    }

    private Optional<Utxo> findPoolUtxo(String txHash, int outputIndex) {
        Optional<Utxo> poolNftUtxo = nftPoolService.findPoolNftUtxo(txHash, outputIndex);
        return poolNftUtxo;
    }

    private Optional<Utxo> findUtxoFromBlockfrost(String txHash, int outputIndex) {
        log.warn("{}:{} could not be found, falling back on blockfrost", txHash, outputIndex);
        try {
            Result<Utxo> txOutput = backendService.getUtxoService().getTxOutput(txHash, outputIndex);
            Optional<Utxo> onchainUtxo;
            if (txOutput.isSuccessful()) {
                onchainUtxo = Optional.of(txOutput.getValue());
            } else {
                onchainUtxo = Optional.empty();
            }
            if (onchainUtxo.isPresent()) {
                log.info("found utxo onchain: {}", onchainUtxo.get());
            } else {
                log.info("{}:{} not found on blockfrost either", txHash, outputIndex);
            }
            return onchainUtxo;
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void extractMempoolUtxo(Transaction tx) {
        String txHash = TransactionUtil.getTxHash(tx);
        List<TransactionOutput> outputs = tx.getBody().getOutputs();
        List<Utxo> utxos = new ArrayList<>();
        for (int i = 0; i < outputs.size(); i++) {
            TransactionOutput transactionOutput = outputs.get(0);
            Utxo utxo = Utxo.builder()
                    .txHash(txHash)
                    .outputIndex(i)
                    .address(transactionOutput.getAddress())
                    .amount(ValueUtil.toAmountList(transactionOutput.getValue()))
                    .dataHash(Optional.ofNullable(transactionOutput.getDatumHash()).map(HexUtil::encodeHexString).orElse(null))
                    .inlineDatum(Optional.ofNullable(transactionOutput.getInlineDatum()).map(PlutusData::serializeToHex).orElse(null))
                    .referenceScriptHash(HexUtil.encodeHexString(transactionOutput.getScriptRef()))
                    .build();
            utxos.add(utxo);
        }
        mempoolTransactions.addAll(utxos);
    }

    public void addUtxoToMempool(Utxo utxo) {
        mempoolTransactions.add(utxo);
    }

    public void addUtxoToWallet(Utxo utxo) {
        walletUtxo.add(utxo);
    }

    public void processBlock(Block block) {
//        log.info("Processed block {}", block.getHeader().getHeaderBody().getSlot());
        block.getTransactionBodies().forEach(tx -> mempoolTransactions.remove(tx.getTxHash()));
        try {
            Result<List<Utxo>> utxos = backendService.getUtxoService().getUtxos(account.baseAddress(), 100, 1);
            walletUtxo.clear();
            walletUtxo.addAll(utxos.getValue());
        } catch (ApiException e) {
            log.error("Could not fetch utxos...");
        }
    }

    private void loadWalletUtxos() {
        log.info("loading wallet utxos");
        try {
            Result<List<Utxo>> utxos = backendService.getUtxoService().getUtxos(account.baseAddress(), 100, 1);
            walletUtxo.clear();
            walletUtxo.addAll(utxos.getValue());
        } catch (Exception e) {
            log.error("Could not fetch utxos...");
        }
    }

    @PostConstruct
    public void loadScriptsReferences() {
        log.info("loading script references");

        List<String> scriptsReferences = List.of(
                // Spectrum
                "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a#2",
                "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a#0",
                "fc9e99fd12a13a137725da61e57a410e36747d513b965993d92c32c67df9259a#1",
                "31a497ef6b0033e66862546aa2928a1987f8db3b8f93c59febbe0f47b14a83c6#0",
                "c8c93656e8bce07fabe2f42d703060b7c71bfa2e48a2956820d1bd81cc936faa#0",
                //Teddy
                "fb6906c2bc39777086036f9c46c297e9d8a41ede154b398d85245a2549b4bf04#0",
                "570f810fe5f8cef730587fb832bb70d8783bad711064d70fc1a378cbefdd7c94#0",
                "e33584ade2b47fb0ab697b63585fb4be935852131643981ba95acde09fe31f41#0",
                "cdafc4e33524e767c4d0ffde094d56fa42105dcfc9b62857974f86fd0e443c32#0"
        );

        scriptsReferences.stream().map(reference -> {
            String[] referenceParts = reference.split("#");
            try {
                return backendService.getUtxoService().getTxOutput(referenceParts[0], Integer.parseInt(referenceParts[1])).getValue();
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
        }).forEach(utxo -> scriptUtxos.add(utxo));

        loadWalletUtxos();

    }

}
