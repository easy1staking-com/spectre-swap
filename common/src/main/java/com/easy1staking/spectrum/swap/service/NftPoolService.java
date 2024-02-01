package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.api.util.ValueUtil;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionOutput;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.yaci.core.model.Block;
import com.bloxbean.cardano.yaci.core.model.TransactionInput;
import com.easy1staking.spectrum.swap.model.*;
import com.easy1staking.spectrum.swap.util.AmountUtil;
import io.adabox.client.OgmiosWSClient;
import io.adabox.model.query.response.UtxoByAddress;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class NftPoolService {

    private static final List<String> IDENTITY_NFT_SUFFIXES = List.of("_IDENTITY", "_NFT");

    @Autowired
    private OgmiosWSClient ogmiosWSClient;
    @Autowired
    private DataParser dataParser;
    private final Map<Amount, Utxo> spectrumPoolV1Map = new ConcurrentHashMap<>();
    private final Map<Amount, Utxo> spectrumPoolV2Map = new ConcurrentHashMap<>();
    private final Map<Amount, Utxo> teddyPoolMap = new ConcurrentHashMap<>();
    private final Map<AssetType, PoolType> nftPoolTypes = new HashMap<>();

    private final List<String> seenTx = new Vector<>();

    @Getter
    private final AtomicLong lastBlockSeenAt = new AtomicLong(System.currentTimeMillis());

    @PostConstruct
    public void startService() throws ApiException {
        teddyPoolMap.keySet().forEach(amount -> nftPoolTypes.put(AssetType.fromUnit(amount.getUnit()), PoolType.TEDDY));
        spectrumPoolV1Map.keySet().forEach(amount -> nftPoolTypes.put(AssetType.fromUnit(amount.getUnit()), PoolType.SPECTRUM_V1));
        spectrumPoolV2Map.keySet().forEach(amount -> nftPoolTypes.put(AssetType.fromUnit(amount.getUnit()), PoolType.SPECTRUM_V2));
    }

    private void populateNftMaps() {
        log.info("about to load nft to utxo/poolV1 map");
        try {
            populateNftMap(List.of(Constants.SPECTRUM_POOL_V1_ADDRESS), spectrumPoolV1Map);
        } catch (ApiException e) {
            log.warn("could not load SPECTRUM_POOL_V1_ADDRESS", e);
        }
        log.info("about to load nft to utxo/poolV2 map");
        try {
            populateNftMap(List.of(Constants.SPECTRUM_POOL_V2_ADDRESS), spectrumPoolV2Map);
        } catch (ApiException e) {
            log.warn("could not load SPECTRUM_POOL_V2_ADDRESS", e);
        }
        log.info("about to load nft to utxo/teddy map");
        try {
            populateNftMap(Constants.TEDDY_POOL_ADDRESSES, teddyPoolMap);
        } catch (ApiException e) {
            log.warn("could not load TEDDY_POOL_ADDRESSES", e);
        }

    }

    public boolean isTeddy(String unit) {
        return teddyPoolMap.keySet().stream().anyMatch(amount -> amount.getUnit().toLowerCase().equalsIgnoreCase(unit));
    }

    public boolean isV1(String unit) {
        boolean isV1 = spectrumPoolV1Map.keySet().stream().anyMatch(amount -> amount.getUnit().toLowerCase().equalsIgnoreCase(unit));
        boolean isV2 = spectrumPoolV2Map.keySet().stream().anyMatch(amount -> amount.getUnit().toLowerCase().equalsIgnoreCase(unit));
        if (isV1 ^ isV2) {
            return isV1;
        } else {
            throw new RuntimeException(String.format("Unexpected situation: isV1 %s isV2 %s", isV1, isV2));
        }
    }

    public Optional<PoolType> getPoolType(String unit) {
        return Optional.ofNullable(nftPoolTypes.get(AssetType.fromUnit(unit)));
    }

    public String unitToDecodedAssetName(String unit) {
        return new String(HexUtil.decodeHexString(unit.substring(56)));
    }

    public Optional<Utxo> findSpectrumPoolUtxoBy(String tokenName) {
        return spectrumPoolV1Map.keySet()
                .stream()
                .filter(amount -> unitToDecodedAssetName(amount.getUnit()).toLowerCase().contains(tokenName.toLowerCase()))
                .findAny()
                .flatMap(amount -> Optional.ofNullable(spectrumPoolV1Map.get(amount)))
                .or(() -> spectrumPoolV2Map.keySet()
                        .stream()
                        .filter(amount -> unitToDecodedAssetName(amount.getUnit()).toLowerCase().contains(tokenName.toLowerCase()))
                        .findAny()
                        .flatMap(amount -> Optional.ofNullable(spectrumPoolV2Map.get(amount))));

    }

    public Optional<Utxo> findTeddyPoolUtxoBy(String tokenName) {
        return teddyPoolMap.keySet()
                .stream()
                .filter(amount -> unitToDecodedAssetName(amount.getUnit()).toLowerCase().contains(tokenName.toLowerCase()))
                .findAny()
                .flatMap(amount -> Optional.ofNullable(teddyPoolMap.get(amount)));
    }

    private void populateNftMap(List<String> addresses, Map<Amount, Utxo> poolMap) throws ApiException {

        addresses.forEach(address -> {
            UtxoByAddress utxoByAddress = ogmiosWSClient.utxoByAddress(address);

            List<Utxo> utxos = utxoByAddress
                    .getUtxos()
                    .stream()
                    .map(utxo -> Utxo.builder()
                            .txHash(utxo.getTxId())
                            .outputIndex(utxo.getIndex().intValue())
                            .address(address)
                            .inlineDatum(utxo.getDatum())
                            .amount(utxo.getAmountList().stream().map(amount -> Amount.builder()
                                    .unit(amount.getUnit().replaceAll("\\.", ""))
                                    .quantity(amount.getQuantity())
                                    .build()).toList())
                            .build())
                    .toList();

            utxos.stream().filter(utxo -> utxo.getInlineDatum() != null).forEach(utxo -> {
                if (utxo.getInlineDatum() == null) {
                    log.warn("utxo {} has no inline datum", utxo);
                }
                utxo.getAmount().stream()
                        .filter(amount -> !amount.getUnit().equals("lovelace"))
                        .filter(amount -> {
                            String assetName = new String(HexUtil.decodeHexString(amount.getUnit().substring(56)));
                            return amount.getQuantity().equals(BigInteger.ONE) &&
                                    IDENTITY_NFT_SUFFIXES.stream().anyMatch(suffix -> assetName.toUpperCase().endsWith(suffix));
                        })
                        .forEach(amount -> poolMap.put(amount, utxo));
            });
        });

    }

    public void updateUtxo(Transaction transaction, String txHash) {

        if (seenTx.contains(txHash)) {
            return;
        }

        seenTx.add(txHash);

        List<TransactionOutput> outputs = transaction
                .getBody()
                .getOutputs();

        for (int i = 0; i < outputs.size(); i++) {
            final int j = i;
            TransactionOutput output = outputs.get(i);
            ValueUtil
                    .toAmountList(output.getValue())
                    .forEach(amount -> getPoolType(amount.getUnit())
                            .ifPresent(poolType -> {
                                Utxo utxo = Utxo.builder()
                                        .txHash(txHash)
                                        .outputIndex(j)
                                        .address(output.getAddress())
                                        .amount(ValueUtil.toAmountList(output.getValue()))
                                        .dataHash(Optional.ofNullable(output.getDatumHash()).map(HexUtil::encodeHexString).orElse(null))
                                        .inlineDatum(Optional.ofNullable(output.getInlineDatum()).map(PlutusData::serializeToHex).orElse(null))
                                        .referenceScriptHash(HexUtil.encodeHexString(output.getScriptRef()))
                                        .build();

                                var map = switch (poolType) {
                                    case SPECTRUM_V1 -> spectrumPoolV1Map;
                                    case SPECTRUM_V2 -> spectrumPoolV2Map;
                                    case TEDDY -> teddyPoolMap;
                                };

//                                log.info("{} size: {} - BEFORE", poolType, map.size());
                                map.put(amount, utxo);
//                                log.info("{} size: {} - AFTER", poolType, map.size());
                            }));
        }


    }

    public Optional<Utxo> getPoolUtxo(AssetType assetType) {
        Amount nftPool = Amount.asset(assetType.toUnit(), BigInteger.ONE);
        return Optional.ofNullable(spectrumPoolV1Map.get(nftPool))
                .or(() -> Optional.ofNullable(spectrumPoolV2Map.get(nftPool)))
                .or(() -> Optional.ofNullable(teddyPoolMap.get(nftPool)));
    }

    public Optional<Utxo> findPoolNftUtxo(String txHash, int index) {
        return spectrumPoolV1Map.values().stream().filter(utxo -> utxo.getTxHash().equals(txHash) && utxo.getOutputIndex() == index)
                .findAny()
                .or(() -> spectrumPoolV2Map.values().stream().filter(utxo -> utxo.getTxHash().equals(txHash) && utxo.getOutputIndex() == index)
                        .findAny())
                .or(() -> teddyPoolMap.values().stream().filter(utxo -> utxo.getTxHash().equals(txHash) && utxo.getOutputIndex() == index)
                        .findAny());
    }

    public void processBlock(Block block) {

        // FIXME: not sure if broken, but utxo state of some pools seems to get stale after some time.

        seenTx.clear();

        log.info("processing block, number: {}, hash: {}", block.getHeader().getHeaderBody().getBlockNumber(),
                block.getHeader().getHeaderBody().getBlockHash());

        lastBlockSeenAt.set(System.currentTimeMillis());

        if (block.getHeader().getHeaderBody().getBlockNumber() % 3 == 0) {

            log.info("reload nft maps from ogmios");

            this.populateNftMaps();

        } else {

            List<String> txHashesToSkip = block.getTransactionBodies().stream().flatMap(tx -> tx.getInputs().stream().map(TransactionInput::getTransactionId)).toList();

            block.getTransactionBodies()
                    .stream()
                    .filter(tx -> !txHashesToSkip.contains(tx.getTxHash()))
                    .forEach(transaction -> {
//                        log.info("Tx: {}", transaction.getTxHash());
                        var outputs = transaction.getOutputs();
                        for (int i = 0; i < outputs.size(); i++) {
                            final int j = i;
                            var output = outputs.get(i);
                            output.getAmounts()
                                    .forEach(amount -> {
                                        try {
                                            var ammPoolType = getPoolType(amount.getUnit());
                                            ammPoolType.ifPresent(poolType -> {
                                                Utxo utxo = Utxo.builder()
                                                        .txHash(transaction.getTxHash())
                                                        .outputIndex(j)
                                                        .address(output.getAddress())
                                                        .amount(output.getAmounts().stream().map(AmountUtil::toAmountCore).toList())
                                                        .dataHash(output.getDatumHash())
                                                        .inlineDatum(output.getInlineDatum())
                                                        .referenceScriptHash(output.getScriptRef())
                                                        .build();

                                                var map = switch (poolType) {
                                                    case SPECTRUM_V1 -> spectrumPoolV1Map;
                                                    case SPECTRUM_V2 -> spectrumPoolV2Map;
                                                    case TEDDY -> teddyPoolMap;
                                                };

                                                var oldUtxo = map.put(AmountUtil.toAmountCore(amount), utxo);

                                                log.info("updating utxo: {} with: {}", oldUtxo, utxo);
                                            });
                                        } catch (Exception e) {
                                            log.warn("foo", e);
                                        }

                                    });
                        }
                    });
        }


    }

    public List<PoolDetails> getAllPoolDetails() {
        List<PoolDetails> poolsDetails = new ArrayList<>();
        buildPoolDetails(spectrumPoolV1Map, poolsDetails, PoolType.SPECTRUM_V1);
        buildPoolDetails(spectrumPoolV2Map, poolsDetails, PoolType.SPECTRUM_V2);
        buildPoolDetails(teddyPoolMap, poolsDetails, PoolType.TEDDY);
        return poolsDetails;
    }

    private void buildPoolDetails(Map<Amount, Utxo> map, List<PoolDetails> poolsDetails, PoolType poolType) {
        map.forEach((amount, utxo) -> {
            var value = utxo.toValue();

            try {
                PoolDatum poolDatum = dataParser.deserializePoolDatum(utxo.getInlineDatum()).get();
                Optional<Amount> tokenY = utxo.getAmount().stream().filter(utxoAmount -> AssetType.fromUnit(utxoAmount.getUnit()).equals(poolDatum.y())).findAny();
                Optional<Amount> nftAmount = utxo.getAmount().stream().filter(utxoAmount -> AssetType.fromUnit(utxoAmount.getUnit()).equals(poolDatum.nft())).findAny();
                if (tokenY.isPresent() && nftAmount.isPresent()) {
                    var amountY = tokenY.get();
                    var nft = nftAmount.get();
                    poolsDetails.add(new PoolDetails(AssetType.ada(),
                            value.getCoin(),
                            AssetType.fromUnit(amountY.getUnit()),
                            amountY.getQuantity(),
                            poolDatum.minX(),
                            poolDatum.feeNum(),
                            AssetType.fromUnit(nft.getUnit()),
                            poolType
                    ));
                } else {
//                    log.info("no tokenY for utxo: {}", utxo);
                }
            } catch (Exception e) {
//                log.info("who cares");
            }


        });
    }


}
