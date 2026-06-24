package com.github.huangguasky.awesometools.core;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RedisLockService implements LockService {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Optional<AwesomeToolLock> tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) {
        String token = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + timeUnit.toNanos(waitTime);
        Duration lease = Duration.ofMillis(timeUnit.toMillis(leaseTime));
        do {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, lease);
            if (Boolean.TRUE.equals(acquired)) {
                return Optional.of(() -> redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token));
            }
            sleepBriefly();
        } while (System.nanoTime() < deadline);
        return Optional.empty();
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
