package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.exception.ApiException;
import com.bloxbean.cardano.client.backend.blockfrost.service.BFBackendService;
import com.bloxbean.cardano.yaci.core.common.NetworkType;
import com.bloxbean.cardano.yaci.core.protocol.chainsync.messages.Point;
import com.bloxbean.cardano.yaci.helper.reactive.BlockStreamer;
import com.easy1staking.spectrum.swap.SpringBaseTest;
import io.adabox.client.OgmiosWSClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class SpectrumNftPoolListener extends SpringBaseTest {

    @Autowired
    BFBackendService bfBackendService;
    @Autowired
    NftPoolService nftPoolService;

    @Test
    public void startPropagating() throws InterruptedException, ApiException {


        var latestBlock = bfBackendService.getBlockService().getLatestBlock().getValue();

        long slot = latestBlock.getSlot();
        String hash = latestBlock.getHash();

        BlockStreamer
                .fromPoint(NetworkType.MAINNET, new Point(slot, hash))
                .stream()
                .subscribe(nftPoolService::processBlock);

        Thread.sleep(999999);
    }


    @Test
    public void testNftPoolUpdates() throws ApiException {


    }

}
