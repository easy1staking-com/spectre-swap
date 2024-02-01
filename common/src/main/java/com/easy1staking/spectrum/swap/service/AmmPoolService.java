package com.easy1staking.spectrum.swap.service;

import com.bloxbean.cardano.client.api.model.Amount;
import com.bloxbean.cardano.client.api.model.Utxo;
import com.easy1staking.spectrum.swap.model.PoolDetails;

import java.util.List;
import java.util.Optional;

public interface AmmPoolService {

    // Quasi-Static list of addresses for Teddy and Spectrum, dynamic for Min
    List<String> getPoolAddresses();

    List<Utxo> getPoolUtxosFor(String address);

    Optional<Amount> getPoolNft(Utxo utxo);

    Optional<PoolDetails> getPoolDetailsFor(Utxo utxo);

}
