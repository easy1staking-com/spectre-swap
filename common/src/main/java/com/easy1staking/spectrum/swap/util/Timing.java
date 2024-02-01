package com.easy1staking.spectrum.swap.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public enum Timing {;

    public static final Map<String, Long> TX_SEEN_TIME = new HashMap<>();

    public static final AtomicLong LAST_TX_SEEN_AT = new AtomicLong(System.currentTimeMillis());

}
