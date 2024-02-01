package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.plutus.spec.PlutusData;
import com.bloxbean.cardano.client.util.HexUtil;
import com.bloxbean.cardano.client.util.JsonUtil;
import com.easy1staking.spectrum.swap.model.AssetType;
import com.easy1staking.spectrum.swap.model.PoolDatum;
import com.easy1staking.spectrum.swap.model.SwapRequestDatum;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class DataParser {

    private final ObjectMapper objectMapper;

    public SwapRequestDatum parseSwapRequestDatum(String datum) throws JsonProcessingException {
        JsonNode jsonNode = JsonUtil.parseJson(datum);

        AssetType base = parseAssetType(jsonNode.path("fields").get(0));
        AssetType quote = parseAssetType(jsonNode.path("fields").get(1));
        AssetType poolNft = parseAssetType(jsonNode.path("fields").get(2));

        BigInteger poolFeeNum = parseNumber(jsonNode.path("fields").get(3));
        BigInteger exFeeNum = parseNumber(jsonNode.path("fields").get(4));
        BigInteger exFeeDen = parseNumber(jsonNode.path("fields").get(5));

        // Payment PKH
        String paymentPkh = jsonNode.path("fields").get(6).path("bytes").asText();
        String stakingPkh = jsonNode.path("fields").get(7).path("fields").get(0).path("bytes").asText();

        BigInteger baseAmount = parseNumber(jsonNode.path("fields").get(8));
        BigInteger minQuoteAmount = parseNumber(jsonNode.path("fields").get(9));

        return new SwapRequestDatum(base, quote, poolNft, poolFeeNum,
                exFeeNum, exFeeDen, paymentPkh, Optional.of(stakingPkh),
                baseAmount, minQuoteAmount);
    }

    private AssetType parseAssetType(JsonNode jsonNode) {
        JsonNode assetTypeNode = jsonNode.path("fields");
        String policyId = assetTypeNode.get(0).path("bytes").asText();
        String assetName = assetTypeNode.get(1).path("bytes").asText();
        return new AssetType(policyId, assetName);
    }

    private BigInteger parseNumber(JsonNode jsonNode) {
        try {
            return new BigInteger(jsonNode.path("int").asText());
        } catch (Exception e) {
            log.info("broken json: {}", jsonNode);
            throw e;
        }
    }


    public Optional<String> deserializeDatum(PlutusData inlineDatum) {
        try {
            return Optional.of(objectMapper.writeValueAsString(inlineDatum));
        } catch (JsonProcessingException e) {
            return Optional.empty();
        }
    }

    public String getNftUnit(String datum) throws JsonProcessingException {
        JsonNode jsonNode = JsonUtil.parseJson(datum);
        JsonNode path = jsonNode.path("fields").get(2).path("fields");
        String policyId = path.get(0).path("bytes").asText();
        String assetName = path.get(1).path("bytes").asText();
        return policyId + assetName;
    }

    public Optional<PoolDatum> deserializePoolDatum(String inlineDatum) throws CborDeserializationException, JsonProcessingException {
        try {
            PlutusData swapDatum = PlutusData.deserialize(HexUtil.decodeHexString(inlineDatum));
            Optional<String> datum = deserializeDatum(swapDatum);

            if (datum.isPresent()) {

                JsonNode jsonNode = JsonUtil.parseJson(datum.get());
                AssetType nft = parseAssetType(jsonNode.path("fields").get(0));
                AssetType y = parseAssetType(jsonNode.path("fields").get(2));
                BigInteger feeNum = parseNumber(jsonNode.path("fields").get(4));
                BigInteger minX = parseNumber(jsonNode.path("fields").get(6));

                return Optional.of(new PoolDatum(AssetType.ada(), y, nft, feeNum, minX));

            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
//            log.warn("can't deserialize pool ");
            return Optional.empty();
        }


    }

}
