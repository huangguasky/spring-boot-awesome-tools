package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class InMemoryRateLimitServiceTest {

    private final InMemoryRateLimitService rateLimitService = new InMemoryRateLimitService();

    @Test
    void tryAcquireAllowsRequestsWithinLimit() {
        assertTrue(rateLimitService.tryAcquire("api:user:1", 2, Duration.ofSeconds(1)));
        assertTrue(rateLimitService.tryAcquire("api:user:1", 2, Duration.ofSeconds(1)));
    }

    @Test
    void tryAcquireRejectsRequestsOverLimit() {
        assertTrue(rateLimitService.tryAcquire("api:user:2", 1, Duration.ofSeconds(1)));

        assertFalse(rateLimitService.tryAcquire("api:user:2", 1, Duration.ofSeconds(1)));
    }

    @Test
    void tryAcquireResetsAfterWindowExpires() throws InterruptedException {
        assertTrue(rateLimitService.tryAcquire("api:user:3", 1, Duration.ofMillis(200)));
        assertFalse(rateLimitService.tryAcquire("api:user:3", 1, Duration.ofMillis(200)));

        Thread.sleep(250);

        assertTrue(rateLimitService.tryAcquire("api:user:3", 1, Duration.ofMillis(200)));
    }
}
