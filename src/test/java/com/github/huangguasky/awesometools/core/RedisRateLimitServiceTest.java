package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class RedisRateLimitServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    private RedisRateLimitService rateLimitService;

    @BeforeEach
    void setUp() {
        rateLimitService = new RedisRateLimitService(redisTemplate);
    }

    @Test
    void tryAcquireAllowsWhenCurrentCountIsWithinLimit() {
        when(redisTemplate.execute(anyScript(), eq(Collections.singletonList("api:1")), eq("1000")))
                .thenReturn(2L);

        assertTrue(rateLimitService.tryAcquire("api:1", 2, Duration.ofSeconds(1)));
    }

    @Test
    void tryAcquireRejectsWhenCurrentCountExceedsLimit() {
        when(redisTemplate.execute(anyScript(), eq(Collections.singletonList("api:2")), eq("1000")))
                .thenReturn(3L);

        assertFalse(rateLimitService.tryAcquire("api:2", 2, Duration.ofSeconds(1)));
    }

    @Test
    void tryAcquireRoundsSubMillisecondWindowUpToOneMillisecond() {
        when(redisTemplate.execute(anyScript(), eq(Collections.singletonList("api:3")), eq("1")))
                .thenReturn(1L);

        assertTrue(rateLimitService.tryAcquire("api:3", 1, Duration.ofNanos(1)));
        verify(redisTemplate).execute(anyScript(), eq(Collections.singletonList("api:3")), eq("1"));
    }

    @SuppressWarnings("unchecked")
    private RedisScript<Long> anyScript() {
        return any(RedisScript.class);
    }
}
