package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.crypto.bip32.HdKeyPair;
import com.bloxbean.cardano.client.exception.CborSerializationException;
import com.bloxbean.cardano.client.transaction.TransactionSigner;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FastTxSigner {

    @Autowired
    private Account account;

    private HdKeyPair hdKeyPair;

    public byte[] sign(Transaction transaction) throws CborSerializationException {
        return TransactionSigner.INSTANCE.sign(transaction.serialize(), hdKeyPair);
    }

    @PostConstruct
    public void initKeys() {
        hdKeyPair = account.hdKeyPair();
    }

}
