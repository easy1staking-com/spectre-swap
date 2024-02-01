package com.easy1staking.spectrum.swap.service;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.CborException;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import com.bloxbean.cardano.aiken.ScriptSupplier;
import com.bloxbean.cardano.client.exception.CborDeserializationException;
import com.bloxbean.cardano.client.plutus.spec.PlutusScript;
import com.bloxbean.cardano.client.plutus.spec.PlutusV2Script;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ScriptProvider implements ScriptSupplier {

    // Spectrum
    @Value("classpath:scripts/spectrum/deposit.uplc")
    private Resource spectrumDeposit;
    @Value("classpath:scripts/spectrum/poolV1.uplc")
    private Resource spectrumPoolV1;
    @Value("classpath:scripts/spectrum/poolV2.uplc")
    private Resource spectrumPoolV2;
    @Value("classpath:scripts/spectrum/redeem.uplc")
    private Resource spectrumRedeem;
    @Value("classpath:scripts/spectrum/swap.uplc")
    private Resource spectrumSwap;

    // Teddy
    @Value("classpath:scripts/teddy/deposit.uplc")
    private Resource teddyDeposit;
    @Value("classpath:scripts/teddy/pool.uplc")
    private Resource teddyPool;
    @Value("classpath:scripts/teddy/redeem.uplc")
    private Resource teddyRedeem;
    @Value("classpath:scripts/teddy/swap.uplc")
    private Resource teddySwap;


    private final Map<String, PlutusScript> scriptsMap = new HashMap<>();

    @PostConstruct
    public void initScriptsMap() throws IOException, CborDeserializationException {
        // Spectrum Deposit
        scriptsMap.put("075e09eb0fa89e1dc34691b3c56a7f437e60ac5ea67b338f2e176e20", createScript(spectrumDeposit));
        // Spectrum Redeem
        scriptsMap.put("83da79f531c19f9ce4d85359f56968a742cf05cc25ed3ca48c302dee", createScript(spectrumRedeem));
        // Spectrum Swap Ref
        scriptsMap.put("2618e94cdb06792f05ae9b1ec78b0231f4b7f4215b1b4cf52e6342de", createScript(spectrumSwap));
        // Spectrum Pool V1
        scriptsMap.put("e628bfd68c07a7a38fcd7d8df650812a9dfdbee54b1ed4c25c87ffbf", createScript(spectrumPoolV1));
        // Spectrum Pool V2
        scriptsMap.put("6b9c456aa650cb808a9ab54326e039d5235ed69f069c9664a8fe5b69", createScript(spectrumPoolV2));

        // Teddy Deposit
        scriptsMap.put("0c70d8047139103546f0e76aafecfdf0667cbb397c8976f40ae8fcb3", createScript(teddyDeposit));
        // Teddy Pool
        scriptsMap.put("28bbd1f7aebb3bc59e13597f333aeefb8f5ab78eda962de1d605b388", createScript(teddyPool));
        // Teddy Redeem
        scriptsMap.put("ab658d65b5717bf07bd3b1a9ad28d31c183811bba4076aeace9feb8e", createScript(teddyRedeem));
        // Teddy Swap
        scriptsMap.put("4ab17afc9a19a4f06b6fe229f9501e727d3968bff03acb1a8f86acf5", createScript(teddySwap));

    }

    private PlutusScript createScript(Resource resource) throws IOException, CborDeserializationException {
        return PlutusV2Script.deserialize(new ByteString(resource.getContentAsByteArray()));
    }

    private PlutusScript createScriptV2(Resource resource) throws IOException, CborDeserializationException, CborException {
        DataItem dataItem = CborDecoder.decode(resource.getContentAsByteArray()).get(0);
        return PlutusV2Script.deserialize((ByteString) dataItem);
    }

    @Override
    public PlutusScript getScript(String scriptHash) {
        PlutusScript plutusScript = scriptsMap.get(scriptHash);
        log.info("requested scriptHash: {}, found {}", scriptHash, plutusScript);
        return plutusScript;
    }

}
