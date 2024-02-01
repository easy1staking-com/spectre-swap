package com.easy1staking.spectrum.swap.config;

import com.bloxbean.cardano.client.account.Account;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class AccountConfig {

    @Bean
    public Account account(@Value("${wallet.mnemonic}") String mnemonic) {
        Account account = new Account(mnemonic);
        log.info("INIT Running account: {}", account.getBaseAddress().getAddress());
        return account;
    }


}
