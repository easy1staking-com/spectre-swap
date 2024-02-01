package com.easy1staking.spectrum.swap.config;

import com.bloxbean.cardano.client.backend.api.TransactionService;
import com.bloxbean.cardano.client.backend.ogmios.OgmiosTransactionService;
import io.adabox.client.OgmiosWSClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@Slf4j
public class OgmiosConfiguration {

    @Bean
    public OgmiosWSClient ogmiosWSClient(@Value("${ogmios.url}") String ogmiosUrl) throws URISyntaxException, InterruptedException {
        log.info("INIT Ogimos url: {}", ogmiosUrl);
        OgmiosWSClient wsClient = new OgmiosWSClient(new URI(ogmiosUrl));
        wsClient.connect();
        Thread.sleep(5000);
        return wsClient;
    }

    @Bean
    public TransactionService transactionService(OgmiosWSClient ogmiosWSClient) {
        return new OgmiosTransactionService(ogmiosWSClient);
    }

}
