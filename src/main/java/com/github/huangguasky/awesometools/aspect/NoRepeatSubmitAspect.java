package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.NoRepeatSubmit;
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
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class NoRepeatSubmitAspect {

    private final ExpiringStore expiringStore;

    private final KeyBuilder keyBuilder;

    public NoRepeatSubmitAspect(ExpiringStore expiringStore, KeyBuilder keyBuilder) {
        this.expiringStore = expiringStore;
        this.keyBuilder = keyBuilder;
    }

    @Around("@annotation(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint joinPoint, NoRepeatSubmit noRepeatSubmit) throws Throwable {
        String key = noRepeatSubmit.key().isBlank()
                ? keyBuilder.methodKey("repeat-submit", joinPoint)
                : keyBuilder.expressionKey("repeat-submit", noRepeatSubmit.key(), joinPoint);
        Duration ttl = Duration.ofMillis(noRepeatSubmit.timeUnit().toMillis(noRepeatSubmit.interval()));
        if (!expiringStore.putIfAbsent(key, ttl)) {
            throw new AwesomeToolsException(noRepeatSubmit.message());
        }
        return joinPoint.proceed();
    }
}
