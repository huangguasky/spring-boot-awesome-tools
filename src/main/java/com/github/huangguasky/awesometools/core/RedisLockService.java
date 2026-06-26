package com.github.huangguasky.awesometools.core;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

public class RedisLockService implements LockService {

    private final RedissonClient redissonClient;

    public RedisLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public Optional<AwesomeToolLock> tryLock(String key, long waitTime, TimeUnit timeUnit) {
        RLock lock = redissonClient.getLock(key);
        boolean acquired;
        try {
            acquired = lock.tryLock(waitTime, timeUnit);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        }
        if (!acquired) {
            return Optional.empty();
        }
        return Optional.of(() -> {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        });
    }
}
