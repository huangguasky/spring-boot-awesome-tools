package com.github.huangguasky.awesometools.aspect;

import java.util.concurrent.Executor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 5)
public class AsyncEventAspect {

    private final Executor executor;

    public AsyncEventAspect(Executor executor) {
        this.executor = executor;
    }

    @Around("@annotation(com.github.huangguasky.awesometools.annotation.AsyncEvent)")
    public Object around(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        if (!Void.TYPE.equals(signature.getReturnType())) {
            throw new IllegalStateException("@AsyncEvent only supports void methods");
        }
        executor.execute(() -> proceed(joinPoint));
        return null;
    }

    private void proceed(ProceedingJoinPoint joinPoint) {
        try {
            joinPoint.proceed();
        } catch (Throwable ex) {
            throw new IllegalStateException("Async event method failed: " + joinPoint.getSignature().toShortString(), ex);
        }
    }
}
