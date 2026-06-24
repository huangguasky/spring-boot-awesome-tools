package com.github.huangguasky.awesometools.cache;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCacheService implements CacheService {

    private final Map<String, Entry> values = new ConcurrentHashMap<>();

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Entry entry = values.get(key);
        if (!isAlive(key, entry)) {
            return Optional.empty();
        }
        return Optional.ofNullable(type.cast(entry.value));
    }

    @Override
    public boolean exists(String key) {
        return isAlive(key, values.get(key));
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        values.put(key, new Entry(value, System.currentTimeMillis() + ttl.toMillis()));
    }

    @Override
    public void delete(String key) {
        values.remove(key);
    }

    private record Entry(Object value, long expiresAt) {
    }

    private boolean isAlive(String key, Entry entry) {
        if (entry == null) {
            return false;
        }
        if (entry.expiresAt <= System.currentTimeMillis()) {
            values.remove(key);
            return false;
        }
        return true;
    }
}
