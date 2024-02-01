package com.easy1staking.spectrum.swap;

import com.bloxbean.cardano.client.api.TransactionEvaluator;
import com.bloxbean.cardano.client.api.TransactionProcessor;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.client.plutus.spec.*;
import com.bloxbean.cardano.client.plutus.spec.serializers.*;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.easy1staking.spectrum.swap.service.DefaultLocalTxSubmissionListener;
import com.easy1staking.spectrum.swap.service.HybridUtxoSupplier;
import com.easy1staking.spectrum.swap.service.NoOpTransactionEvaluator;
import com.easy1staking.spectrum.swap.service.StaticProtocolParamsSupplier;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.adabox.client.OgmiosWSClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootTest(classes = {SpringBaseTest.TestConfig.class, HybridUtxoSupplier.class})
@ActiveProfiles("test")
public class SpringBaseTest {

    @Configuration
    @ComponentScan(basePackages = {"com.easy1staking.spectrum.swap"})
    static class TestConfig {

        @Value("${blockfrost.key}")
        private String blockfrostKey;

        @Bean
        @Primary
        public BFBackendService bfBackendService() {
            return new BFBackendService(com.bloxbean.cardano.client.backend.blockfrost.common.Constants.BLOCKFROST_MAINNET_URL, blockfrostKey);
        }

//        @Bean
//        @Primary
//        public TransactionProcessor localTransactionProcessor(@Autowired TransactionEvaluator aikenTransactionEvaluator) {
//            return new LocalTransactionProcessor(null, aikenTransactionEvaluator);
//        }

        @Bean
        public LocalClientProvider socatLocalClientProvider(@Value("${cardano.node.host}") String cardanoNodeHost,
                                                            @Value("${cardano.node.socatPort}") int cardanoNodeSocatPort) {
            LocalClientProvider localClientProvider = new LocalClientProvider(cardanoNodeHost, cardanoNodeSocatPort, Constants.MAINNET_PROTOCOL_MAGIC);
            localClientProvider.addTxSubmissionListener(new DefaultLocalTxSubmissionListener("NODE"));
            localClientProvider.start();
            return localClientProvider;
        }

        @Bean
        public LocalTxSubmissionClient socketLocalTxSubmissionClient(@Autowired LocalClientProvider localClientProvider) {
            return localClientProvider.getTxSubmissionClient();
        }

        @Bean
        public TransactionEvaluator TransactionEvaluator() {
            return new NoOpTransactionEvaluator();
        }

        @Bean
        public QuickTxBuilder quickTxBuilder(HybridUtxoSupplier hybridUtxoSupplier,
                                             StaticProtocolParamsSupplier staticProtocolParamsSupplier,
                                             TransactionProcessor transactionProcessor) {
            return new QuickTxBuilder(hybridUtxoSupplier,
                    staticProtocolParamsSupplier,
                    transactionProcessor
            );
        }

        @Bean
        public OgmiosWSClient ogmiosWSClient(@Value("${ogmios.url}") String ogmiosUrl) throws InterruptedException, URISyntaxException {
            OgmiosWSClient ogmiosWSClient = new OgmiosWSClient(new URI(ogmiosUrl));
            ogmiosWSClient.connect();
            Thread.sleep(5000L);
            return ogmiosWSClient;
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(ConstrPlutusData.class, new ConstrDataJsonSerializer());
            module.addSerializer(BigIntPlutusData.class, new BigIntDataJsonSerializer());
            module.addSerializer(BytesPlutusData.class, new BytesDataJsonSerializer());
            module.addSerializer(ListPlutusData.class, new ListDataJsonSerializer());
            module.addSerializer(MapPlutusData.class, new MapDataJsonSerializer());
            objectMapper.registerModule(module);
            return objectMapper;
        }


    }


}
