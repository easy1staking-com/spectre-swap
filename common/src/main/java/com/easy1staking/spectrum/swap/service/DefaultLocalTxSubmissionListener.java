package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.yaci.core.protocol.localtx.LocalTxSubmissionListener;
import com.bloxbean.cardano.yaci.core.protocol.localtx.messages.MsgAcceptTx;
import com.bloxbean.cardano.yaci.core.protocol.localtx.messages.MsgRejectTx;
import com.bloxbean.cardano.yaci.core.protocol.localtx.model.TxSubmissionRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultLocalTxSubmissionListener implements LocalTxSubmissionListener {

    private final String context;

    public DefaultLocalTxSubmissionListener(String context) {
        this.context = context;
    }

    @Override
    public void txAccepted(TxSubmissionRequest txSubmissionRequest, MsgAcceptTx msgAcceptTx) {
        log.info("{} Accepted: {}", context, txSubmissionRequest.getTxHash());
    }

    @Override
    public void txRejected(TxSubmissionRequest txSubmissionRequest, MsgRejectTx msgRejectTx) {
        log.info("{} Rejected: {}", context, txSubmissionRequest.getTxHash());
    }
}
