package com.github.huangguasky.awesometools.core;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

public class RedisLockService implements LockService {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private static final DefaultRedisScript<Long> RENEW_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end",
            Long.class);

    private final StringRedisTemplate redisTemplate;

    private final ScheduledExecutorService renewalExecutor;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.renewalExecutor = Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
    }

    @Override
    public Optional<AwesomeToolLock> tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) {
        return tryLock(key, waitTime, leaseTime, timeUnit, false);
    }

    @Override
    public Optional<AwesomeToolLock> tryLock(
            String key, long waitTime, long leaseTime, TimeUnit timeUnit, boolean renewLease) {
        String token = UUID.randomUUID().toString();
        long deadline = System.nanoTime() + timeUnit.toNanos(waitTime);
        Duration lease = toRedisLeaseDuration(leaseTime, timeUnit);
        do {
            Boolean acquired = redisTemplate.opsForValue().setIfAbsent(key, token, lease);
            if (Boolean.TRUE.equals(acquired)) {
                ScheduledFuture<?> renewalTask = renewLease ? scheduleRenewal(key, token, lease) : null;
                return Optional.of(() -> {
                    if (renewalTask != null) {
                        renewalTask.cancel(false);
                    }
                    redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), token);
                });
            }
            sleepBriefly();
        } while (System.nanoTime() < deadline);
        return Optional.empty();
    }

    private Duration toRedisLeaseDuration(long leaseTime, TimeUnit timeUnit) {
        long leaseNanos = timeUnit.toNanos(leaseTime);
        long leaseMillis = Math.max(1, (leaseNanos + 999_999) / 1_000_000);
        return Duration.ofMillis(leaseMillis);
    }

    private ScheduledFuture<?> scheduleRenewal(String key, String token, Duration lease) {
        long leaseMillis = Math.max(1, lease.toMillis());
        long renewalInterval = Math.max(1, leaseMillis / 3);
        return renewalExecutor.scheduleWithFixedDelay(
                () -> renewLease(key, token, leaseMillis),
                renewalInterval,
                renewalInterval,
                TimeUnit.MILLISECONDS);
    }

    private void renewLease(String key, String token, long leaseMillis) {
        try {
            redisTemplate.execute(RENEW_SCRIPT, Collections.singletonList(key), token, String.valueOf(leaseMillis));
        } catch (RuntimeException ignored) {
            // Keep the renewal task alive for transient Redis failures.
        }
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static class DaemonThreadFactory implements ThreadFactory {

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "awesome-tools-redis-lock-renewal");
            thread.setDaemon(true);
            return thread;
        }
    }
}
