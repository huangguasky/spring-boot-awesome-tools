package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class RedisExpiringStoreTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisExpiringStore store;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new RedisExpiringStore(redisTemplate);
    }

    @Test
    void putIfAbsentReturnsTrueOnlyWhenRedisSetsTheKey() {
        when(valueOperations.setIfAbsent("request:1", "1", Duration.ofSeconds(1))).thenReturn(true, false);

        assertTrue(store.putIfAbsent("request:1", Duration.ofSeconds(1)));
        assertFalse(store.putIfAbsent("request:1", Duration.ofSeconds(1)));
    }

    @Test
    void deleteRemovesRedisKey() {
        store.delete("request:2");

        verify(redisTemplate).delete("request:2");
    }
}
