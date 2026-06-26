package com.github.huangguasky.awesometools.core;

import com.github.huangguasky.awesometools.annotation.RateLimitType;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryRateLimitService implements RateLimitService {

    private final Map<String, LocalRateLimiter> limiters = new ConcurrentHashMap<>();

    @Override
    public boolean tryAcquire(String key, long limit, Duration window, RateLimitType type) {
        if (limit <= 0 || window.isZero() || window.isNegative()) {
            return false;
        }
        String limiterKey = type + ":" + key;
        LocalRateLimiter limiter = limiters.compute(limiterKey, (ignored, existing) -> {
            if (existing == null || existing.type != type) {
                return new LocalRateLimiter(type);
            }
            return existing;
        });
        synchronized (limiter) {
            return limiter.tryAcquire(limit, window);
        }
    }

    private static class LocalRateLimiter {

        private final RateLimitType type;

        private long fixedWindowExpiresAt;

        private long fixedWindowCount;

        private final Deque<Long> slidingWindowRequests = new ArrayDeque<>();

        private long lastLeakTime;

        private double water;

        private long lastRefillTime;

        private double tokens;

        private LocalRateLimiter(RateLimitType type) {
            this.type = type;
        }

        private boolean tryAcquire(long limit, Duration window) {
            long now = System.nanoTime();
            long windowNanos = Math.max(1, window.toNanos());
            return switch (type) {
                case FIXED_WINDOW -> tryFixedWindow(now, limit, windowNanos);
                case SLIDING_WINDOW -> trySlidingWindow(now, limit, windowNanos);
                case LEAKY_BUCKET -> tryLeakyBucket(now, limit, windowNanos);
                case TOKEN_BUCKET -> tryTokenBucket(now, limit, windowNanos);
            };
        }

        /**
         * Counts requests inside a fixed window and resets the counter when the window expires.
         */
        private boolean tryFixedWindow(long now, long limit, long windowNanos) {
            if (fixedWindowExpiresAt <= now) {
                fixedWindowExpiresAt = now + windowNanos;
                fixedWindowCount = 0;
            }
            if (fixedWindowCount >= limit) {
                return false;
            }
            fixedWindowCount++;
            return true;
        }

        /**
         * Keeps a queue of request timestamps and only counts entries still inside the rolling window.
         */
        private boolean trySlidingWindow(long now, long limit, long windowNanos) {
            long earliest = now - windowNanos;
            while (!slidingWindowRequests.isEmpty() && slidingWindowRequests.peekFirst() <= earliest) {
                slidingWindowRequests.pollFirst();
            }
            if (slidingWindowRequests.size() >= limit) {
                return false;
            }
            slidingWindowRequests.addLast(now);
            return true;
        }

        /**
         * Drains accumulated water at a constant rate and rejects requests when the bucket is full.
         */
        private boolean tryLeakyBucket(long now, long limit, long windowNanos) {
            if (lastLeakTime == 0) {
                lastLeakTime = now;
            }
            double leaked = (double) (now - lastLeakTime) * limit / windowNanos;
            water = Math.max(0, water - leaked);
            lastLeakTime = now;
            if (water + 1 > limit) {
                return false;
            }
            water++;
            return true;
        }

        /**
         * Refills tokens at a constant rate and accepts a request only when a token is available.
         */
        private boolean tryTokenBucket(long now, long limit, long windowNanos) {
            if (lastRefillTime == 0) {
                lastRefillTime = now;
                tokens = limit;
            }
            double refilled = (double) (now - lastRefillTime) * limit / windowNanos;
            tokens = Math.min(limit, tokens + refilled);
            lastRefillTime = now;
            if (tokens < 1) {
                return false;
            }
            tokens--;
            return true;
        }
    }
}
