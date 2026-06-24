package com.github.huangguasky.awesometools.core;

import java.time.Duration;

public interface RateLimitService {

    boolean tryAcquire(String key, long limit, Duration window);
}
