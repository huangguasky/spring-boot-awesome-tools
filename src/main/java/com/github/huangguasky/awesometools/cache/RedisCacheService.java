package com.github.huangguasky.awesometools.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;

public class RedisCacheService implements CacheService {

    private final StringRedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    public RedisCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(objectMapper.readValue(value, type));
        } catch (Exception ex) {
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void put(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), ttl);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to serialize cache value", ex);
        }
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
