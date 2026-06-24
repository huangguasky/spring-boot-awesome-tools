package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.Idempotent;
import com.github.huangguasky.awesometools.core.ExpiringStore;
import com.github.huangguasky.awesometools.core.KeyBuilder;
import com.github.huangguasky.awesometools.exception.AwesomeToolsException;
import java.time.Duration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class IdempotentAspect {

    private final ExpiringStore expiringStore;

    private final KeyBuilder keyBuilder;

    public IdempotentAspect(ExpiringStore expiringStore, KeyBuilder keyBuilder) {
        this.expiringStore = expiringStore;
        this.keyBuilder = keyBuilder;
    }

    @Around("@annotation(idempotent)")
    public Object around(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {
        String key = keyBuilder.expressionKey("idempotent", idempotent.key(), joinPoint);
        Duration ttl = Duration.ofMillis(idempotent.timeUnit().toMillis(idempotent.expireTime()));
        if (!expiringStore.putIfAbsent(key, ttl)) {
            throw new AwesomeToolsException(idempotent.message());
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            expiringStore.delete(key);
            throw ex;
        }
    }
}
