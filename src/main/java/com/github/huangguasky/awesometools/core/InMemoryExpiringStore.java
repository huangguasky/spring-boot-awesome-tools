package com.github.huangguasky.awesometools.core;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryExpiringStore implements ExpiringStore {

    private final Map<String, Long> values = new ConcurrentHashMap<>();

    @Override
    public boolean putIfAbsent(String key, Duration ttl) {
        long expiresAt = System.currentTimeMillis() + ttl.toMillis();
        values.entrySet().removeIf(entry -> entry.getValue() <= System.currentTimeMillis());
        return values.putIfAbsent(key, expiresAt) == null;
    }

    @Override
    public void delete(String key) {
        values.remove(key);
    }
}
