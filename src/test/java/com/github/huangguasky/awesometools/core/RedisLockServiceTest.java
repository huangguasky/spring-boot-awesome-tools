package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

@ExtendWith(MockitoExtension.class)
class RedisLockServiceTest {

    private static final String LOCK_KEY = "lock:test";

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private RLock lock;

    private RedisLockService lockService;

    @BeforeEach
    void setUp() {
        when(redissonClient.getLock(LOCK_KEY)).thenReturn(lock);
        lockService = new RedisLockService(redissonClient);
    }

    @Test
    void tryLockUsesRedissonWatchdog() throws InterruptedException {
        when(lock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);

        Optional<AwesomeToolLock> result = lockService.tryLock(LOCK_KEY, 10, TimeUnit.SECONDS);

        assertTrue(result.isPresent());
        verify(lock).tryLock(10, TimeUnit.SECONDS);
        verify(lock, never()).tryLock(10, 30, TimeUnit.SECONDS);
    }

    @Test
    void tryLockReturnsEmptyWhenRedissonCannotAcquireLock() throws InterruptedException {
        when(lock.tryLock(10, TimeUnit.SECONDS)).thenReturn(false);

        Optional<AwesomeToolLock> result = lockService.tryLock(LOCK_KEY, 10, TimeUnit.SECONDS);

        assertTrue(result.isEmpty());
    }

    @Test
    void tryLockReturnsEmptyAndRestoresInterruptWhenInterrupted() throws InterruptedException {
        when(lock.tryLock(10, TimeUnit.SECONDS)).thenThrow(new InterruptedException());

        Optional<AwesomeToolLock> result = lockService.tryLock(LOCK_KEY, 10, TimeUnit.SECONDS);

        assertTrue(result.isEmpty());
        assertTrue(Thread.currentThread().isInterrupted());
        Thread.interrupted();
    }

    @Test
    void closeUnlocksWhenCurrentThreadStillOwnsLock() throws InterruptedException {
        when(lock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        Optional<AwesomeToolLock> result = lockService.tryLock(LOCK_KEY, 10, TimeUnit.SECONDS);

        result.get().close();

        verify(lock).unlock();
    }

    @Test
    void closeDoesNotUnlockWhenCurrentThreadNoLongerOwnsLock() throws InterruptedException {
        when(lock.tryLock(10, TimeUnit.SECONDS)).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(false);

        Optional<AwesomeToolLock> result = lockService.tryLock(LOCK_KEY, 10, TimeUnit.SECONDS);

        result.get().close();

        verify(lock, never()).unlock();
    }
}
