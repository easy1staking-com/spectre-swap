package com.easy1staking.spectrum.swap.config;

import com.bloxbean.cardano.client.api.TransactionProcessor;
import com.bloxbean.cardano.client.quicktx.QuickTxBuilder;
import com.bloxbean.cardano.yaci.core.common.Constants;
import com.bloxbean.cardano.yaci.helper.LocalClientProvider;
import com.bloxbean.cardano.yaci.helper.LocalTxSubmissionClient;
import com.easy1staking.spectrum.swap.service.DefaultLocalTxSubmissionListener;
import com.easy1staking.spectrum.swap.service.DefaultTxMonitorListener;
import com.easy1staking.spectrum.swap.service.HybridUtxoSupplier;
import com.easy1staking.spectrum.swap.service.StaticProtocolParamsSupplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@Profile("!test")
@Slf4j
@EnableScheduling
public class YaciConfiguration {

    @Bean
    @Primary
    @Profile("socat")
    public LocalClientProvider socatLocalClientProvider(@Value("${cardano.node.host}") String cardanoNodeHost,
                                                        @Value("${cardano.node.socatPort}") int cardanoNodeSocatPort) {
        log.info("INIT Cardano node: {}:{}", cardanoNodeHost, cardanoNodeSocatPort);
        LocalClientProvider localClientProvider = new LocalClientProvider(cardanoNodeHost, cardanoNodeSocatPort, Constants.MAINNET_PROTOCOL_MAGIC);
        localClientProvider.addTxSubmissionListener(new DefaultLocalTxSubmissionListener("NODE"));
        localClientProvider.addLocalTxMonitorListener(new DefaultTxMonitorListener());
        localClientProvider.start();
        return localClientProvider;
    }

    @Bean
    @Profile("default")
    public LocalClientProvider socketLocalClientProvider(@Value("${cardano.node.socket.path}") String cardanoNodeSocketPath) {
        log.info("INIT Cardano node socket: {}", cardanoNodeSocketPath);
        LocalClientProvider localClientProvider = new LocalClientProvider(cardanoNodeSocketPath, Constants.MAINNET_PROTOCOL_MAGIC);
        localClientProvider.addTxSubmissionListener(new DefaultLocalTxSubmissionListener("NODE"));
        localClientProvider.addLocalTxMonitorListener(new DefaultTxMonitorListener());
        localClientProvider.start();
        return localClientProvider;
    }

    @Bean
    public LocalTxSubmissionClient socketLocalTxSubmissionClient(@Autowired LocalClientProvider localClientProvider) {
        return localClientProvider.getTxSubmissionClient();
    }

    @Bean
    public QuickTxBuilder quickTxBuilder(HybridUtxoSupplier hybridUtxoSupplier,
                                         StaticProtocolParamsSupplier staticProtocolParamsSupplier,
                                         TransactionProcessor transactionProcessor) {
        log.info("INIT TransactionProcessor: {}", transactionProcessor);
        return new QuickTxBuilder(hybridUtxoSupplier,
                staticProtocolParamsSupplier,
                transactionProcessor
        );
    }

}
