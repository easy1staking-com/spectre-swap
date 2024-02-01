package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.yaci.core.protocol.localtxmonitor.LocalTxMonitorListener;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultTxMonitorListener implements LocalTxMonitorListener {


    @Override
    public void onDisconnect() {
        log.info("onDisconnect");
    }

}
