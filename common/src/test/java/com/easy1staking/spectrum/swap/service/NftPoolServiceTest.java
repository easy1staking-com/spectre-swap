package com.easy1staking.spectrum.swap.service;

import com.easy1staking.spectrum.swap.model.PoolDetails;
import com.easy1staking.spectrum.swap.model.PoolType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.adabox.client.OgmiosWSClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@SpringBootTest(classes = {NftPoolServiceTest.TestConfig.class, NftPoolService.class, DataParser.class})
@ActiveProfiles("test")
class NftPoolServiceTest {

    @Configuration
    static class TestConfig {

        @Bean
        public OgmiosWSClient ogmiosWSClient(@Value("${ogmios.url}") String ogmiosUrl) throws URISyntaxException, InterruptedException {
            OgmiosWSClient ogmiosWSClient = new OgmiosWSClient(new URI(ogmiosUrl));
            ogmiosWSClient.connect();
            Thread.sleep(5000L);
            return ogmiosWSClient;
        }

        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

    }

    @Autowired
    private NftPoolService nftPoolService;

    @Test
    public void doIt() {

        List<PoolDetails> teddyPools = nftPoolService
                .getAllPoolDetails()
                .stream()
                .filter(poolDetails -> poolDetails.poolType().equals(PoolType.TEDDY))
                .toList();

        teddyPools.forEach(pool -> log.info("Toke: {}", pool.yType().unsafeHumanAssetName()));

    }


}