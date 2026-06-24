package com.github.huangguasky.awesometools.core;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryRateLimitService implements RateLimitService {

    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, long limit, Duration window) {
        long now = System.currentTimeMillis();
        Counter counter = counters.compute(key, (ignored, existing) -> {
            if (existing == null || existing.expiresAt <= now) {
                return new Counter(now + window.toMillis());
            }
            return existing;
        });
        return counter.count.incrementAndGet() <= limit;
    }

    private static class Counter {

        private final AtomicLong count = new AtomicLong();

        private final long expiresAt;

        private Counter(long expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}
