package com.easy1staking.spectrum.swap.model;

import com.easy1staking.spectrum.swap.model.AssetType;

import java.util.List;
import java.util.Map;

public class Constants {

    public static final String SPECTRUM_SWAP_ADDRESS = "addr1wynp362vmvr8jtc946d3a3utqgclfdl5y9d3kn849e359hsskr20n";
    public static final String SPECTRUM_POOL_V1_ADDRESS = "addr1x8nz307k3sr60gu0e47cmajssy4fmld7u493a4xztjrll0aj764lvrxdayh2ux30fl0ktuh27csgmpevdu89jlxppvrswgxsta";
    public static final String SPECTRUM_POOL_V2_ADDRESS = "addr1x94ec3t25egvhqy2n265xfhq882jxhkknurfe9ny4rl9k6dj764lvrxdayh2ux30fl0ktuh27csgmpevdu89jlxppvrst84slu";
    public static final String TEDDY_SWAP_ADDRESS = "addr1z99tz7hungv6furtdl3zn72sree86wtghlcr4jc637r2eadkp2avt5gp297dnxhxcmy6kkptepsr5pa409qa7gf8stzs0706a3";
    public static final List<String> SWAP_ADDRESSES = List.of(SPECTRUM_SWAP_ADDRESS, TEDDY_SWAP_ADDRESS);

    public static final List<String> TEDDY_POOL_ADDRESSES = List.of(
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zr0vp2360e2j2gve54sxsheawjd6s6we2d25xl96a3r0jdqzvyqkl", //SNEK
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8z9kp2avt5gp297dnxhxcmy6kkptepsr5pa409qa7gf8stzsxg8sx3",  // CNETA, TEDY, CHRY
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zqgdzhkv23nm3v7tanurzu8v5vll365n7hq8f26937hatlqnv5cpz",  // LENFI
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8z9re630pc4dzmhtku8276tyq0glgn53h93vw5rl9e6w4g8su86xvk", // OPTIM
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zzlsgmhduch9juwcjf6vjqeht0jv2g2mlz86wqh42h8akdqglnguu", // ENCS
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zxn5qy8sn2d7wtdtvjcsv7v0h7u9zsleljxv3nschr5sj3sla73t7", //cBTC
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zxk96389hhwyhv0t07gh89wqnaqg9cqkwsz4esd9sm562rs55tl66",  //iETH
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zphr7r6v67asj5jc5w5uapfapv0u9433m3v9aag9w46spaqc60ygw", // INDY
            "addr1qxhwefhsv6xn2s4sn8a92f9m29lwj67aykn4plr9xal4r48del5pz2hf795j5wxzhzf405g377jmw7a92k9z2enhd6pqlal6jy", // Optim??
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8z92v2k4gz85r5rq035n2llzemqvcz70h7hdr3njur05y6nsmrsjpe", // iBTC
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zrlxa5g3cwp6thfvzwhd9s4vcjjdwttsss65l09dum7g9rs0mr8px", // iUSD
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zp5hu6t748dfdd6cxlxxssyqez4wqwcrq44crfgkltqh2cqcwcjyr", // DJED
            "addr1zy5th50h46anh3v7zdvh7ve6amac7k4h3mdfvt0p6czm8zz6mve63ntrqp7yxgkk395rngtzdmzdjzzuzdkdks0afwqsmdsegq" // FACT
    );

    public static final Map<AssetType, AssetType> POOL_NFT_PAIRS = Map.of(
            // LENFI - AADA
            AssetType.fromUnit("a22ebe57c45d0be3ba4bebca5a9d4877b42d7fd872f3d740414fa124414144415f4144415f4e4654"),
            AssetType.fromUnit("c30d7086eeb68050a5b01efc219c5d4b5d5fd38e2e62fd6d7f01ac4d414144415f4144415f504f4f4c5f4944454e54495459")
    );


}
