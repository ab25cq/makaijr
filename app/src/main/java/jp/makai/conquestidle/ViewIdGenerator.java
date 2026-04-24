package com.makaijr;

import java.util.concurrent.atomic.AtomicInteger;

final class ViewIdGenerator {
    private static final AtomicInteger NEXT = new AtomicInteger(1);

    private ViewIdGenerator() {
    }

    static int next() {
        return NEXT.getAndIncrement();
    }
}
