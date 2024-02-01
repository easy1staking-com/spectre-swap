# Spectre Swap

Original haskell swap code https://github.com/spectrum-finance/cardano-dex-backend

## Open Questions


### How to calculate the amount

### How to calculate if swap is possible

### How co calculate batcher fees

### Find meaning of the Redeemer for the swap

### Dec 2nd

Spectrum Finance scripts redeemers contain indexes of input index and outputs.

These indexes are used to perform some validation. See notes Dec 1st about self index of Redeemer Swap and Indexes of inputs
and outputs of Redeemer of pool.

1. Are input UTXO sorted lexographically? https://github.com/input-output-hk/plutus/issues/4296#issuecomment-1032842887
2. is there a way to control/influence the order of outputs?
2. This tx https://cexplorer.io/tx/d81577589ab3227ccdc66141467804595976b45e2689720f8466667bf59c421c is resolved with tx id: 109c73da6241c1fffa6e5eb8e16bec64c44c2bf7d9df72a78f5a662e895ab5b8

### Dec 1st

Pool Redeemer
``` 
pcon' Deposit = 0
    pcon' Redeem = 1
    pcon' Swap = 2
    pcon' Destroy = 3
    pcon' ChangeStakingPool = 4
                '[ "action" ':= PoolAction
                 , "selfIx" ':= PInteger
                 ]
```

SelfIx is the input index of the swap uxto


Swap Redeemer
```
  pcon' Apply  = 0
    pcon' Refund = 1
                '[ "poolInIx" ':= PInteger
                 , "orderInIx" ':= PInteger
                 , "rewardOutIx" ':= PInteger
                 , "action" ':= OrderAction
                 ]
```


### November 30

Output amount definition: https://github.com/spectrum-finance/cardano-dex-sdk-haskell/blob/39d53d78fce1cf20b78b10bff9a9fc73b747cf2a/dex-core/src/ErgoDex/Amm/Pool.hs#L234

```
outputAmount :: Pool -> AssetAmount Base -> AssetAmount Quote
outputAmount Pool{poolFee=PoolFee{..}, ..} (AssetAmount baseAsset (Amount baseAmount)) =
    if xy then
      assetAmountCoinOf (retagCoin poolCoinY)
        ((poolReservesY' * baseAmount * poolFeeNum') `div` (poolReservesX' * poolFeeDen' + baseAmount * poolFeeNum'))
    else
      assetAmountCoinOf (retagCoin poolCoinX)
        ((poolReservesX' * baseAmount * poolFeeNum') `div` (poolReservesY' * poolFeeDen' + baseAmount * poolFeeNum'))
  where
    xy             = unCoin baseAsset == unCoin poolCoinX
    poolReservesX' = unAmount poolReservesX
    poolReservesY' = unAmount poolReservesY
```

The where xy possibly means something like, if a pool has two tokens, X and Y, if the baseAsset (policy of the base asset), 
is the token X of the pool's pair. Coz you can swap X -> Y and Y -> X. So just to get the proportion right.

### November 29

Possible swap implementation in haskell
https://github.com/spectrum-finance/cardano-dex-sdk-haskell/blob/master/dex-core/src/ErgoDex/Amm/PoolActions.hs#L95-L152

More interesting stuff
https://github.com/spectrum-finance/cardano-dex-sdk-haskell/blob/master/dex-core/src/ErgoDex/Amm/Orders.hs#L40

### November 28

Good reading about how Pool/Swap/Orders work on Spectrum

https://github.com/easy1staking-com/cardano-dex-contracts/blob/master/docs/CardanoAmmDex.md

* PPool.hs, for swaps redeemer is Redeemer(2, 0) https://github.com/easy1staking-com/cardano-dex-contracts/blob/master/cardano-dex-contracts-onchain/ErgoDex/PContracts/PPool.hs#L68-L103

### November 27

This looks like it contains definition for all the items of the Datum for a swap:

https://github.com/easy1staking-com/cardano-dex-contracts/blob/master/cardano-dex-contracts-offchain/ErgoDex/Contracts/Proxy/Swap.hs#L38-L49

Given a pair of tokens: A and B where user wants to swap A for B, A it's called base, B it's called quote.

* base :: AssetClass, type of token to exchange
* quote :: AssetClass, type of token to receive 
* poolNft :: AssetClass, the NFT that uniquely identifies the pool (can be either in a V1 or V2 contract see below)
* feeNum :: Integer, it should be the pool fee (Each AMM pool has a fee. this is usually 997 which should mean 0.3% )
* exFeePerTokenNum :: Integer
* exFeePerTokenDen :: Integer
* rewardPkh :: PubKeyHash, this and the one below are used to reconstruct customer address where to return funds (this is payment address PKH)
* stakePkh :: Maybe PubKeyHash (this is optional PKH of stake address)
* baseAmount :: Integer (amount of token A sent )
* minQuoteAmount :: Integer (min amount of token B accepted )

#### Execution costs

Looks like execution costs are hard coded to:
* "mem": 270000 / "steps": 140000000
* "mem": 530000 / "steps": 165000000

Not sure which one is the pool ref input and which and which is the swap ref inputs.

#### Pool V1 or V2

A possible way of understanding which version of the pool it is, it's to check in which utxo the NFT of the pool sits:
* v1 for uxto under addr1x8nz307k3sr60gu0e47cmajssy4fmld7u493a4xztjrll0aj764lvrxdayh2ux30fl0ktuh27csgmpevdu89jlxppvrswgxsta
* v2 for uxto under addr1x94ec3t25egvhqy2n265xfhq882jxhkknurfe9ny4rl9k6dj764lvrxdayh2ux30fl0ktuh27csgmpevdu89jlxppvrst84slu


## Objectives

* Implement offchain swap code
* Make it CPU light
* Target is 1ms processing time
* 
* Implement other offchain-code
* Experiment about TX submission and validation (if a swap tx is already in mempool, will my tx be rejected?)
* Play around with node topology: one relay receives tx, another is isolated, can we just bridge swap request tx to 
isolated node and submit swap tx there?