package com.github.huangguasky.awesometools.cache;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {

    <T> Optional<T> get(String key, Class<T> type);

    boolean exists(String key);

    void put(String key, Object value, Duration ttl);

    void delete(String key);
}
