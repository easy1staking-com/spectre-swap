package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.api.model.Result;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.easy1staking.spectrum.swap.SpringBaseTest;
import com.easy1staking.spectrum.swap.model.SwapResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@Slf4j
class SwapServiceSimulationTest extends SpringBaseTest {

    @Autowired
    private BFBackendService bfBackendService;

    @Autowired
    private SwapService swapService;

    @Test
    public void canFetchSpentUtxoFromBF() throws ApiException {
        Result<Utxo> txOutput = bfBackendService
                .getUtxoService()
                .getTxOutput("3e3260b67f3f1cf57a84906a1ba7830f8ffca491fd21f067b0c0a4c7fa897395", 0);

        boolean isSuccessful = txOutput.isSuccessful();
        log.info("isSuccessful: {}", isSuccessful);

        log.info("utxo: {}", txOutput.getValue());
    }

    @Test
    public void replaySwapRequest_0b9dc() throws Exception {

        Result<Utxo> txOutput = bfBackendService
                .getUtxoService()
                .getTxOutput("0b9dc74fcf80f2c4a4d5c78843f6e0f74b60ffe26018911465c3d23cb6a0b7ec", 0);

        Result<Utxo> poolUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("0942d16e20781303e6113ea1ee0c103cc30756c13535cb11bc80eed31e5e97fc", 0);

        boolean isSuccessful = txOutput.isSuccessful();
        log.info("isSuccessful: {}", isSuccessful);

        log.info("utxo: {}", txOutput.getValue());

        swapService.executeSwap(txOutput.getValue(), 1000l, Optional.of(poolUtxo.getValue()), Optional.empty());
    }

    @Test
    public void replaySwapRequest_WorkingExample() throws Exception {

        Result<Utxo> poolUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("432b9a7c1a66744026c2b2bf5d89ab3cd99d5bd88934cd886052b2400ae463d3", 0);

        Result<Utxo> swapUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("56852a881ac19d72e25b51349c023e672122a8835594ee35d43ee84f14d5571d", 0);

        log.info("poolUtxo: {}", poolUtxo.getValue());
        log.info("swapUtxo: {}", swapUtxo.getValue());

        swapService.executeSwap(swapUtxo.getValue(), 1000l, Optional.of(poolUtxo.getValue()), Optional.empty());

    }

    @Test
    public void replaySwapRequest_31f1e4() throws Exception {

//        31f1e4ead0af2d2d73b3a36ef8b076c9c41ced3a0cca09359af3d662b43839a6

        Result<Utxo> poolUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("90854557a354c32360a10bcf7bebbe951589fd0bb2f6f65fc99c4d25fd93c51b", 0);

        Result<Utxo> swapUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("d88812db4cc4dd4a3c5ac92827a8ee70b2b886b2fc26eb66ef5427fba6f69b00", 0);

        log.info("poolUtxo: {}", poolUtxo.getValue());
        log.info("swapUtxo: {}", swapUtxo.getValue());

        Optional<SwapResult> swapResults = Optional.empty();
//        var swapResults = Optional.of(new SwapResult(476532378L, 1530000));

        swapService.executeSwap(swapUtxo.getValue(), 1000l, Optional.of(poolUtxo.getValue()), swapResults);

    }

    @Test
    public void replaySwapRequest_a152be05() throws Exception {

//        a152be05588caf2d71ebcf7fc4095b3c4971ec19777704a1cbd64ed61dfc5729


//        poolUtxo: Utxo(txHash=5dfbc4b4f0fc2fe8799119b25744925b0391ccdc4d63c1c03825a8cd0a32be29, outputIndex=0,
//        address=addr1x8nz307k3sr60gu0e47cmajssy4fmld7u493a4xztjrll0aj764lvrxdayh2ux30fl0ktuh27csgmpevdu89jlxppvrswgxsta,
//        amount=[Amount(unit=lovelace, quantity=320254455576),
//        Amount(unit=5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d114494147, quantity=847379010203),
//        Amount(unit=a780d696f101b64dd436a3c419c8c7742d3047f605d60f28837b0abe4941475f4144415f4c51, quantity=9223371534561706845),
//        Amount(unit=b992582b95a3ee20cb4025699808c83caaefa7bae9387b72ba2c57c34941475f4144415f4e4654, quantity=1)],
//        dataHash=null,
//        inlineDatum=d8799fd8799f581cb992582b95a3ee20cb4025699808c83caaefa7bae9387b72ba2c57c34b4941475f4144415f4e4654ffd8799f4040ffd8799f581c5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d11443494147ffd8799f581ca780d696f101b64dd436a3c419c8c7742d3047f605d60f28837b0abe4a4941475f4144415f4c51ff1903e59f581c84a0503f8c7574d4ca4f4336e705aa2018b25808ea146cd5ec2032c3ff1b00000004a817c800ff, referenceScriptHash=null)2023-12-18T19:31:53.906Z  INFO 1 --- [ntLoopGroup-3-1] c.e.s.s.s.SwapTransactionSubmitter       :
//
//        swapRequestUtxo: Utxo(txHash=80569ff25214360509a5c33c3f6949c0e8723e5628a0aaefc6c6fb6aba8ae466, outputIndex=0,
//        address=addr1wynp362vmvr8jtc946d3a3utqgclfdl5y9d3kn849e359hsskr20n,
//        amount=[Amount(unit=lovelace, quantity=3800000),
//        Amount(unit=5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d114494147, quantity=1000000000)],
//        dataHash=null, inlineDatum=d8799fd8799f581c5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d11443494147ffd8799f4040ffd8799f581cb992582b95a3ee20cb4025699808c83caaefa7bae9387b72ba2c57c34b4941475f4144415f4e4654ff1903e51b0000045f1ec79fd11b00038d7ea4c68000581cc8eb8b8c95252972c9de332f248df19e0186a82eca073862857ab77bd8799f581c8d5bbf2c20c106868553135b9edf448ff5b8a96634b71a4b2e25e287ff1a3b9aca001a165234fbff, referenceScriptHash=null)2023-12-18T19:31:53.906Z  INFO 1 --- [ntLoopGroup-3-1] c.e.s.s.s.SwapTransactionSubmitter       :



//        poolUtxo: Utxo(txHash=5dfbc4b4f0fc2fe8799119b25744925b0391ccdc4d63c1c03825a8cd0a32be29, outputIndex=0,
//        address=addr1x8nz307k3sr60gu0e47cmajssy4fmld7u493a4xztjrll0aj764lvrxdayh2ux30fl0ktuh27csgmpevdu89jlxppvrswgxsta,
//        amount=[Amount(unit=lovelace, quantity=320254455576),
//        Amount(unit=5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d114494147, quantity=847379010203),
//        Amount(unit=a780d696f101b64dd436a3c419c8c7742d3047f605d60f28837b0abe4941475f4144415f4c51, quantity=9223371534561706845)
//        , Amount(unit=b992582b95a3ee20cb4025699808c83caaefa7bae9387b72ba2c57c34941475f4144415f4e4654, quantity=1)],
//        dataHash=91c122b589ec44aaf7b6fe56e0d8281b7edd665ed6dacad9fd7efb8b2a286fa9, inlineDatum=d8799fd8799f581cb992582b95a3ee20cb4025699808c83caaefa7bae9387b72ba2c57c34b4941475f4144415f4e4654ffd8799f4040ffd8799f581c5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d11443494147ffd8799f581ca780d696f101b64dd436a3c419c8c7742d3047f605d60f28837b0abe4a4941475f4144415f4c51ff1903e59f581c84a0503f8c7574d4ca4f4336e705aa2018b25808ea146cd5ec2032c3ff1b00000004a817c800ff, referenceScriptHash=null)
//
//        2023-12-18T19:35:32.456Z  INFO 90480 --- [    Test worker] c.e.s.s.s.SwapServiceSimulationTest      :
//        swapUtxo: Utxo(txHash=80569ff25214360509a5c33c3f6949c0e8723e5628a0aaefc6c6fb6aba8ae466, outputIndex=0,
//        address=addr1wynp362vmvr8jtc946d3a3utqgclfdl5y9d3kn849e359hsskr20n,
//        amount=[Amount(unit=lovelace, quantity=3800000),
//        Amount(unit=5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d114494147, quantity=1000000000)],
//        dataHash=26f1c190167e94625af56156c8285454ab625189e18d9c2df5a711220b5363f0, inlineDatum=d8799fd8799f581c5d16cc1a177b5d9ba9cfa9793b07e60f1fb70fea1f8aef064415d11443494147ffd8799f4040ffd8799f581cb992582b95a3ee20cb4025699808c83caaefa7bae9387b72ba2c57c34b4941475f4144415f4e4654ff1903e51b0000045f1ec79fd11b00038d7ea4c68000581cc8eb8b8c95252972c9de332f248df19e0186a82eca073862857ab77bd8799f581c8d5bbf2c20c106868553135b9edf448ff5b8a96634b71a4b2e25e287ff1a3b9aca001a165234fbff, referenceScriptHash=null)


        Result<Utxo> poolUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("5dfbc4b4f0fc2fe8799119b25744925b0391ccdc4d63c1c03825a8cd0a32be29", 0);

        Result<Utxo> swapUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("80569ff25214360509a5c33c3f6949c0e8723e5628a0aaefc6c6fb6aba8ae466", 0);

        log.info("poolUtxo: {}", poolUtxo.getValue());
        log.info("swapUtxo: {}", swapUtxo.getValue());

        Optional<SwapResult> swapResults = Optional.empty();
//        var swapResults = Optional.of(new SwapResult(476532378L, 1530000));

        swapService.executeSwap(swapUtxo.getValue(), 1000l, Optional.of(poolUtxo.getValue()), swapResults);

    }

    @Test
    public void replaySwap_ValueNotConserved() throws ApiException {


//        pool value: Value(coin=1774510510464, multiAssets=[MultiAsset{policyId=cab2059b754430ae6e09a547f94d61de11901573f1d2388de95cbb0c, assets=[Asset{name=0x48554e545f4144415f4e4654, value=1}]}, MultiAsset{policyId=95a427e384527065f2f8946f5e86320d0117839a5e98ea2c0b55fb00, assets=[Asset{name=0x48554e54, value=2942602341576}]}, MultiAsset{policyId=3c89cd4df26d20c652441c24b989a1290872fb2ba94eea20a785a477, assets=[Asset{name=0x48554e545f4144415f4c51, value=9223369832245942736}]}])2023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        vase: 26000000002023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        isBaseX: true2023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        reserveX: 17745105104642023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        reserveY: 29426023415762023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        feeNum: 9852023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        feeDen: 10002023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        exFeeNum: 3572891388023412023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        exFeeDen: 10000000000000000002023-12-18T20:08:56.714Z  INFO 1 --- [ntLoopGroup-3-1] c.e.spectrum.swap.service.SwapService    :
//        SwapResult: SwapResult[quote=4240687306, batcherFee=1515151]

        Result<Utxo> poolUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("87c63754fb9082f0046da2fa08bfe06aaf5218392a3fc49854d2d255a9172ebb", 1);

        Result<Utxo> swapUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("9ec654c84f2402dd90d9819a72842c487d1171bf66aae9538f2987e5f5ce300c", 0);

        log.info("poolUtxo: {}", poolUtxo.getValue());
        log.info("swapUtxo: {}", swapUtxo.getValue());
    }


    @Test
    public void replayTeddySwapRequest_31f1e4() throws Exception {

//        31f1e4ead0af2d2d73b3a36ef8b076c9c41ced3a0cca09359af3d662b43839a6

        Result<Utxo> poolUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("41d2177ac4a6566f731a80a445c47c58c4f31a96e4cddd35c972a1c5dfccaad2", 0);

        Result<Utxo> swapUtxo = bfBackendService
                .getUtxoService()
                .getTxOutput("e7a30dce0bb3d2939f49b1ded69f9d2f395cab29df94690b3e83fd72ad90e98e", 0);

        log.info("poolUtxo: {}", poolUtxo.getValue());
        log.info("swapUtxo: {}", swapUtxo.getValue());

        Optional<SwapResult> swapResults = Optional.empty();
//        var swapResults = Optional.of(new SwapResult(476532378L, 1530000));

        swapService.executeSwap(swapUtxo.getValue(), 1000l, Optional.of(poolUtxo.getValue()), swapResults);

    }



}