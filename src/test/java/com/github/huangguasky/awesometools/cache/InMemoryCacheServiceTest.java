package com.github.huangguasky.awesometools.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InMemoryCacheServiceTest {

    private final InMemoryCacheService cacheService = new InMemoryCacheService();

    @Test
    void getReturnsCachedValueBeforeTtlExpires() {
        cacheService.put("user:1", "Alice", Duration.ofSeconds(1));

        Optional<String> value = cacheService.get("user:1", String.class);

        assertTrue(value.isPresent());
        assertEquals("Alice", value.get());
        assertTrue(cacheService.exists("user:1"));
    }

    @Test
    void getRemovesExpiredValue() throws InterruptedException {
        cacheService.put("user:2", "Bob", Duration.ofMillis(5));

        Thread.sleep(20);

        assertTrue(cacheService.get("user:2", String.class).isEmpty());
        assertFalse(cacheService.exists("user:2"));
    }

    @Test
    void deleteRemovesCachedValue() {
        cacheService.put("user:3", "Carol", Duration.ofSeconds(1));

        cacheService.delete("user:3");

        assertTrue(cacheService.get("user:3", String.class).isEmpty());
        assertFalse(cacheService.exists("user:3"));
    }
}
