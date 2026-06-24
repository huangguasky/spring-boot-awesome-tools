package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.DistributedLock;
import com.github.huangguasky.awesometools.core.AwesomeToolLock;
import com.github.huangguasky.awesometools.core.KeyBuilder;
import com.github.huangguasky.awesometools.core.LockService;
import com.github.huangguasky.awesometools.exception.AwesomeToolsException;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DistributedLockAspect {

    private final LockService lockService;

    private final KeyBuilder keyBuilder;

    public DistributedLockAspect(LockService lockService, KeyBuilder keyBuilder) {
        this.lockService = lockService;
        this.keyBuilder = keyBuilder;
    }

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String key = keyBuilder.expressionKey("lock", distributedLock.key(), joinPoint);
        Optional<AwesomeToolLock> lock = lockService.tryLock(
                key, distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
        if (lock.isEmpty()) {
            throw new AwesomeToolsException(distributedLock.message());
        }
        try (AwesomeToolLock ignored = lock.get()) {
            return joinPoint.proceed();
        }
    }
}
