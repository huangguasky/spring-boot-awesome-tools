package com.github.huangguasky.awesometools.core;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class InMemoryLockService implements LockService {

    private final ConcurrentMap<String, ReentrantLock> locks = new ConcurrentHashMap<>();

    @Override
    public Optional<AwesomeToolLock> tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit) {
        ReentrantLock lock = locks.computeIfAbsent(key, ignored -> new ReentrantLock());
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
            lock.unlock();
            if (!lock.isLocked() && !lock.hasQueuedThreads()) {
                locks.remove(key, lock);
            }
        });
    }
}
