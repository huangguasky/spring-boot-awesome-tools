package com.github.huangguasky.awesometools.core;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface LockService {

    Optional<AwesomeToolLock> tryLock(String key, long waitTime, long leaseTime, TimeUnit timeUnit);

    default Optional<AwesomeToolLock> tryLock(
            String key, long waitTime, long leaseTime, TimeUnit timeUnit, boolean renewLease) {
        return tryLock(key, waitTime, leaseTime, timeUnit);
    }
}
