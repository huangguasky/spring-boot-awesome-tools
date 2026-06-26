package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class InMemoryLockServiceTest {

    private final InMemoryLockService lockService = new InMemoryLockService();

    @Test
    void tryLockRejectsSameKeyFromAnotherThreadUntilLockIsClosed() throws ExecutionException, InterruptedException {
        Optional<AwesomeToolLock> firstLock = lockService.tryLock("order:1", 0, TimeUnit.SECONDS);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        assertTrue(firstLock.isPresent());
        try {
            Future<Optional<AwesomeToolLock>> competingLock =
                    executor.submit(() -> lockService.tryLock("order:1", 0, TimeUnit.SECONDS));

            assertTrue(competingLock.get().isEmpty());
        } finally {
            executor.shutdownNow();
        }

        firstLock.get().close();

        assertTrue(lockService.tryLock("order:1", 0, TimeUnit.SECONDS).isPresent());
    }

}
