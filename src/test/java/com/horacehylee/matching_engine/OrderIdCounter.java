package com.horacehylee.matching_engine;

import java.util.concurrent.atomic.AtomicLong;

public final class OrderIdCounter {

    private static final AtomicLong counter = new AtomicLong();

    public static void reset() {
        counter.set(0L);
    }

    public static long get() {
        return counter.incrementAndGet();
    }
}
