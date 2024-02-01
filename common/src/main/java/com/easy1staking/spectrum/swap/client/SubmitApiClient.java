package com.easy1staking.spectrum.swap.client;

import com.bloxbean.cardano.client.transaction.spec.Transaction;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class SubmitApiClient {

    private static record TxSubmitResponse(String text, boolean isError) {
    }

    @Value("${submitApi.url}")
    private String submitApiUrl;

    private WebClient client;

    public void submit(Transaction transaction) {
        try {
            submit(transaction.serialize());
        } catch (Exception e) {
            log.warn("can't submit transaction", e);
        }
    }

    public void submit(byte[] txBytes) {
        client.post()
                .bodyValue(txBytes)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.ACCEPTED)) {
                        return response.bodyToMono(String.class).map(txHash -> new TxSubmitResponse(txHash, false));
                    } else if (response.statusCode().is4xxClientError()) {
                        return response.bodyToMono(String.class).map(txHash -> new TxSubmitResponse(txHash, true));
                    } else {
                        return Mono.just(new TxSubmitResponse("Unknown Error", true));
                    }
                })
                .subscribe(txSubmitResponse -> {
                    if (txSubmitResponse.isError()) {
                        log.info("Tx Submission Error: {}", txSubmitResponse.text());
                    } else {
                        log.info("Tx Submitted {}", txSubmitResponse.text);
                    }
                });

    }


    @PostConstruct
    public void initWebClient() {
        log.info("INIT setting submit api client url to: {}", submitApiUrl);
        client = WebClient.builder()
                .baseUrl(String.format("%s/api/submit/tx", submitApiUrl))
                .defaultHeader("Content-Type", "application/cbor")
                .build();
    }


}
