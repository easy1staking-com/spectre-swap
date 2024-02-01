package com.easy1staking.spectrum.swap.controller;

import com.easy1staking.spectrum.swap.service.NftPoolService;
import com.easy1staking.spectrum.swap.util.Timing;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/__internal__/healthcheck")
@Slf4j
@RequiredArgsConstructor
public class Healthcheck {

    private static final long START_TIME = System.currentTimeMillis();

    private final NftPoolService nftPoolService;

    @RequestMapping(value = "/", method = GET, produces = "application/json")
    public ResponseEntity<?> healthcheck() {

        boolean notReceivingBlocks = false;
        boolean notReceivingTxs = false;

        long now = System.currentTimeMillis();
        long lastBlockSeenAt = nftPoolService.getLastBlockSeenAt().get();

        long timeSinceLastBlock = now - lastBlockSeenAt;
        if (timeSinceLastBlock > 2 * 60 * 1000L) {
            notReceivingBlocks = true;
        }

        long timeOfLastTxSeen = Timing.LAST_TX_SEEN_AT.get();
        long timeSinceLastTx = now - timeOfLastTxSeen;
        if (timeSinceLastTx > 60 * 1000L) {
            notReceivingTxs = true;
        }

        if (notReceivingBlocks || notReceivingTxs) {
            log.error("Last tx age {}s, last block age {}s",
                    timeSinceLastTx / 1000,
                    timeSinceLastBlock / 1000);
            return ResponseEntity.internalServerError().build();
        } else {
            return ResponseEntity.ok().build();
        }

    }

}
