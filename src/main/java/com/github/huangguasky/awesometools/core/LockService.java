package com.github.huangguasky.awesometools.core;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface LockService {

    Optional<AwesomeToolLock> tryLock(String key, long waitTime, TimeUnit timeUnit);
}
