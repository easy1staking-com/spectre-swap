package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.account.Account;
import io.adabox.client.OgmiosWSClient;
import io.adabox.model.query.response.UtxoByAddress;
import io.adabox.model.query.response.models.Utxo;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AccountService {

    @Value("${wallet.collateral.hash}")
    private Optional<String> collateralHashOpt;
    @Value("${wallet.collateral.index}")
    private Optional<Integer> collateralIndexOpt;
    @Autowired
    private OgmiosWSClient ogmiosWSClient;
    @Autowired
    private Account account;

    @Getter
    private String collateralHash;
    @Getter
    private int collateralIndex;

    @PostConstruct
    public void init() {
        if (collateralHashOpt.isPresent() && collateralIndexOpt.isPresent()) {
            collateralHash = collateralHashOpt.get();
            collateralIndex = collateralIndexOpt.get();
            log.info("INIT using collateral from config: {}#{}", collateralHash, collateralIndex);
        } else {
            UtxoByAddress utxoByAddress = ogmiosWSClient.utxoByAddress(account.baseAddress());
            Optional<Utxo> collateralOpt = utxoByAddress.getUtxos()
                    .stream()
                    .filter(utxo -> utxo.getAmountList()
                            .stream()
                            .anyMatch(amount -> amount.getUnit().equals("lovelace") &&
                                    amount.getQuantity().longValue() >= 5000000L && amount.getQuantity().longValue() <= 10000000L
                            ))
                    .findAny();
            if (collateralOpt.isEmpty()) {
                throw new RuntimeException(String.format("no valid collateral in the wallet, send 5 ada to %s", account.baseAddress()));
            } else {
                collateralHash = collateralOpt.get().getTxId();
                collateralIndex = collateralOpt.get().getIndex().intValue();
            }
            log.info("INIT using collateral from ogmios: {}#{}", collateralHash, collateralIndex);
        }
    }


}
