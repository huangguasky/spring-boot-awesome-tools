package com.github.huangguasky.awesometools.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisCacheService cacheService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cacheService = new RedisCacheService(redisTemplate, new ObjectMapper());
    }

    @Test
    void getReturnsEmptyWhenRedisValueIsMissing() {
        when(valueOperations.get("user:1")).thenReturn(null);

        assertTrue(cacheService.get("user:1", User.class).isEmpty());
    }

    @Test
    void getDeserializesRedisValue() {
        when(valueOperations.get("user:2")).thenReturn("{\"name\":\"Alice\"}");

        Optional<User> user = cacheService.get("user:2", User.class);

        assertTrue(user.isPresent());
        assertEquals("Alice", user.get().name());
    }

    @Test
    void getDeletesCacheAndReturnsEmptyWhenValueCannotBeDeserialized() {
        when(valueOperations.get("user:3")).thenReturn("not-json");

        assertTrue(cacheService.get("user:3", User.class).isEmpty());
        verify(redisTemplate).delete("user:3");
    }

    @Test
    void existsDelegatesToRedisHasKey() {
        when(redisTemplate.hasKey("user:4")).thenReturn(true);

        assertTrue(cacheService.exists("user:4"));
    }

    @Test
    void putSerializesValueWithTtl() {
        cacheService.put("user:5", new User("Bob"), Duration.ofSeconds(1));

        verify(valueOperations).set("user:5", "{\"name\":\"Bob\"}", Duration.ofSeconds(1));
    }

    @Test
    void putThrowsWhenValueCannotBeSerialized() {
        assertThrows(IllegalStateException.class, () -> cacheService.put("bad", new Object() {
            @SuppressWarnings("unused")
            public Object getSelf() {
                return this;
            }
        }, Duration.ofSeconds(1)));
    }

    @Test
    void deleteRemovesRedisKey() {
        cacheService.delete("user:6");

        verify(redisTemplate).delete("user:6");
    }

    private record User(String name) {
    }
}
