package com.github.huangguasky.awesometools.core;

import java.time.Duration;

public interface ExpiringStore {

    boolean putIfAbsent(String key, Duration ttl);

    void delete(String key);
}
