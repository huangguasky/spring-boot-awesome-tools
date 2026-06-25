package com.github.huangguasky.awesometools.core;

import java.time.Duration;
import java.util.Collections;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RedisRateLimitService implements RateLimitService {

    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            "local current = redis.call('incr', KEYS[1]); "
                    + "if current == 1 then redis.call('pexpire', KEYS[1], ARGV[1]); end; "
                    + "return current;",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean tryAcquire(String key, long limit, Duration window) {
        Long current = redisTemplate.execute(SCRIPT, Collections.singletonList(key), String.valueOf(toRedisMillis(window)));
        return current != null && current <= limit;
    }

    private long toRedisMillis(Duration window) {
        long nanos = window.toNanos();
        return Math.max(1, (nanos + 999_999) / 1_000_000);
    }
}
