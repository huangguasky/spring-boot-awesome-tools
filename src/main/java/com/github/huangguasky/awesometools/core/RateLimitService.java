package com.github.huangguasky.awesometools.core;

import com.github.huangguasky.awesometools.annotation.RateLimitType;
import java.time.Duration;

public interface RateLimitService {

    boolean tryAcquire(String key, long limit, Duration window, RateLimitType type);
}
