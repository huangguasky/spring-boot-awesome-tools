package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

@ExtendWith(MockitoExtension.class)
class RedisLockServiceTest {

    private static final String LOCK_KEY = "lock:test";

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private RedisLockService lockService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lockService = new RedisLockService(redisTemplate);
    }

    @Test
    void tryLockReturnsLockAndUnlocksWithSameToken() {
        when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(Duration.ofMillis(100))))
                .thenReturn(true);

        Optional<AwesomeToolLock> lock = lockService.tryLock(LOCK_KEY, 10, 100, TimeUnit.MILLISECONDS);

        assertTrue(lock.isPresent());
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).setIfAbsent(eq(LOCK_KEY), tokenCaptor.capture(), eq(Duration.ofMillis(100)));

        lock.get().close();

        verify(redisTemplate)
                .execute(
                        argThat((RedisScript<Long> script) -> isUnlockScript(script)),
                        eq(Collections.singletonList(LOCK_KEY)),
                        eq(tokenCaptor.getValue()));
    }

    @Test
    void tryLockReturnsEmptyWhenRedisKeyCannotBeSetWithinWaitTime() {
        when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(Duration.ofMillis(100))))
                .thenReturn(false);

        Optional<AwesomeToolLock> lock = lockService.tryLock(LOCK_KEY, 0, 100, TimeUnit.MILLISECONDS);

        assertTrue(lock.isEmpty());
        verify(valueOperations).setIfAbsent(eq(LOCK_KEY), anyString(), eq(Duration.ofMillis(100)));
    }

    @Test
    void tryLockRoundsSubMillisecondLeaseUpToOneMillisecond() {
        when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(Duration.ofMillis(1))))
                .thenReturn(true);

        Optional<AwesomeToolLock> lock = lockService.tryLock(LOCK_KEY, 10, 1, TimeUnit.MICROSECONDS);

        assertTrue(lock.isPresent());
        verify(valueOperations).setIfAbsent(eq(LOCK_KEY), anyString(), eq(Duration.ofMillis(1)));
        lock.get().close();
    }

    @Test
    void tryLockDoesNotRenewLeaseByDefault() throws InterruptedException {
        when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(Duration.ofMillis(30))))
                .thenReturn(true);

        Optional<AwesomeToolLock> lock = lockService.tryLock(LOCK_KEY, 10, 30, TimeUnit.MILLISECONDS);

        assertTrue(lock.isPresent());
        Thread.sleep(80);

        verify(redisTemplate, never())
                .execute(
                        argThat((RedisScript<Long> script) -> isRenewScript(script)),
                        eq(Collections.singletonList(LOCK_KEY)),
                        anyString(),
                        eq("30"));
        lock.get().close();
    }

    @Test
    void tryLockRenewsLeaseWhenRenewLeaseIsEnabled() {
        when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(Duration.ofMillis(30))))
                .thenReturn(true);

        Optional<AwesomeToolLock> lock = lockService.tryLock(LOCK_KEY, 10, 30, TimeUnit.MILLISECONDS, true);

        assertTrue(lock.isPresent());
        verify(redisTemplate, timeout(300).atLeastOnce())
                .execute(
                        argThat((RedisScript<Long> script) -> isRenewScript(script)),
                        eq(Collections.singletonList(LOCK_KEY)),
                        anyString(),
                        eq("30"));

        lock.get().close();

        verify(redisTemplate, atLeastOnce())
                .execute(
                        argThat((RedisScript<Long> script) -> isUnlockScript(script)),
                        eq(Collections.singletonList(LOCK_KEY)),
                        anyString());
    }

    @Test
    void tryLockCancelsLeaseRenewalWhenLockIsClosed() throws InterruptedException {
        when(valueOperations.setIfAbsent(eq(LOCK_KEY), anyString(), eq(Duration.ofMillis(60))))
                .thenReturn(true);

        Optional<AwesomeToolLock> lock = lockService.tryLock(LOCK_KEY, 10, 60, TimeUnit.MILLISECONDS, true);

        assertTrue(lock.isPresent());
        verify(redisTemplate, timeout(300).atLeastOnce())
                .execute(
                        argThat((RedisScript<Long> script) -> isRenewScript(script)),
                        eq(Collections.singletonList(LOCK_KEY)),
                        anyString(),
                        eq("60"));

        lock.get().close();
        int renewalCountAfterClose = countRenewals();
        Thread.sleep(100);

        assertEquals(renewalCountAfterClose, countRenewals());
    }

    private int countRenewals() {
        return mockingDetails(redisTemplate).getInvocations().stream()
                .filter(invocation -> "execute".equals(invocation.getMethod().getName()))
                .filter(invocation -> invocation.getArguments().length == 4)
                .filter(invocation -> isRenewScript(invocation.getArgument(0)))
                .toList()
                .size();
    }

    private static boolean isUnlockScript(RedisScript<?> script) {
        return script != null && script.getScriptAsString().contains("del");
    }

    private static boolean isRenewScript(RedisScript<?> script) {
        return script != null && script.getScriptAsString().contains("pexpire");
    }
}
