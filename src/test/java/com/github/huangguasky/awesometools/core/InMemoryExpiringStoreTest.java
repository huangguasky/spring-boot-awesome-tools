package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class InMemoryExpiringStoreTest {

    private final InMemoryExpiringStore store = new InMemoryExpiringStore();

    @Test
    void putIfAbsentRejectsExistingLiveKey() {
        assertTrue(store.putIfAbsent("request:1", Duration.ofSeconds(1)));

        assertFalse(store.putIfAbsent("request:1", Duration.ofSeconds(1)));
    }

    @Test
    void putIfAbsentAllowsExpiredKeyAgain() throws InterruptedException {
        assertTrue(store.putIfAbsent("request:2", Duration.ofMillis(5)));

        Thread.sleep(20);

        assertTrue(store.putIfAbsent("request:2", Duration.ofSeconds(1)));
    }

    @Test
    void deleteAllowsKeyToBeReservedAgain() {
        assertTrue(store.putIfAbsent("request:3", Duration.ofSeconds(1)));

        store.delete("request:3");

        assertTrue(store.putIfAbsent("request:3", Duration.ofSeconds(1)));
    }
}
