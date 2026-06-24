package com.github.huangguasky.awesometools.core;

import java.time.Duration;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisExpiringStore implements ExpiringStore {

    private final StringRedisTemplate redisTemplate;

    public RedisExpiringStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean putIfAbsent(String key, Duration ttl) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
