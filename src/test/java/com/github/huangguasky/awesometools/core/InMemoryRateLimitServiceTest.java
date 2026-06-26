package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.huangguasky.awesometools.annotation.RateLimitType;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class InMemoryRateLimitServiceTest {

    private final InMemoryRateLimitService rateLimitService = new InMemoryRateLimitService();

    @Test
    void fixedWindowAllowsRequestsWithinLimit() {
        assertTrue(rateLimitService.tryAcquire("api:user:1", 2, Duration.ofSeconds(1), RateLimitType.FIXED_WINDOW));
        assertTrue(rateLimitService.tryAcquire("api:user:1", 2, Duration.ofSeconds(1), RateLimitType.FIXED_WINDOW));
    }

    @Test
    void fixedWindowRejectsRequestsOverLimit() {
        assertTrue(rateLimitService.tryAcquire("api:user:2", 1, Duration.ofSeconds(1), RateLimitType.FIXED_WINDOW));

        assertFalse(rateLimitService.tryAcquire("api:user:2", 1, Duration.ofSeconds(1), RateLimitType.FIXED_WINDOW));
    }

    @Test
    void fixedWindowResetsAfterWindowExpires() throws InterruptedException {
        assertTrue(rateLimitService.tryAcquire("api:user:3", 1, Duration.ofMillis(200), RateLimitType.FIXED_WINDOW));
        assertFalse(rateLimitService.tryAcquire("api:user:3", 1, Duration.ofMillis(200), RateLimitType.FIXED_WINDOW));

        Thread.sleep(250);

        assertTrue(rateLimitService.tryAcquire("api:user:3", 1, Duration.ofMillis(200), RateLimitType.FIXED_WINDOW));
    }

    @Test
    void slidingWindowRejectsUntilOldRequestsLeaveWindow() throws InterruptedException {
        assertTrue(rateLimitService.tryAcquire("api:user:4", 2, Duration.ofMillis(200), RateLimitType.SLIDING_WINDOW));
        assertTrue(rateLimitService.tryAcquire("api:user:4", 2, Duration.ofMillis(200), RateLimitType.SLIDING_WINDOW));
        assertFalse(rateLimitService.tryAcquire("api:user:4", 2, Duration.ofMillis(200), RateLimitType.SLIDING_WINDOW));

        Thread.sleep(250);

        assertTrue(rateLimitService.tryAcquire("api:user:4", 2, Duration.ofMillis(200), RateLimitType.SLIDING_WINDOW));
    }

    @Test
    void leakyBucketRejectsWhenBucketIsFullAndAllowsAfterLeak() throws InterruptedException {
        assertTrue(rateLimitService.tryAcquire("api:user:5", 2, Duration.ofMillis(200), RateLimitType.LEAKY_BUCKET));
        assertTrue(rateLimitService.tryAcquire("api:user:5", 2, Duration.ofMillis(200), RateLimitType.LEAKY_BUCKET));
        assertFalse(rateLimitService.tryAcquire("api:user:5", 2, Duration.ofMillis(200), RateLimitType.LEAKY_BUCKET));

        Thread.sleep(120);

        assertTrue(rateLimitService.tryAcquire("api:user:5", 2, Duration.ofMillis(200), RateLimitType.LEAKY_BUCKET));
    }

    @Test
    void tokenBucketRejectsWhenTokensAreEmptyAndAllowsAfterRefill() throws InterruptedException {
        assertTrue(rateLimitService.tryAcquire("api:user:6", 2, Duration.ofMillis(200), RateLimitType.TOKEN_BUCKET));
        assertTrue(rateLimitService.tryAcquire("api:user:6", 2, Duration.ofMillis(200), RateLimitType.TOKEN_BUCKET));
        assertFalse(rateLimitService.tryAcquire("api:user:6", 2, Duration.ofMillis(200), RateLimitType.TOKEN_BUCKET));

        Thread.sleep(120);

        assertTrue(rateLimitService.tryAcquire("api:user:6", 2, Duration.ofMillis(200), RateLimitType.TOKEN_BUCKET));
    }

    @Test
    void rejectsInvalidLimitOrWindow() {
        assertFalse(rateLimitService.tryAcquire("api:user:7", 0, Duration.ofSeconds(1), RateLimitType.FIXED_WINDOW));
        assertFalse(rateLimitService.tryAcquire("api:user:8", 1, Duration.ZERO, RateLimitType.TOKEN_BUCKET));
    }
}
