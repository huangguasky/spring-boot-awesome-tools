package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.RetryableTask;
import com.github.huangguasky.awesometools.core.ThrowableMatcher;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 30)
public class RetryableTaskAspect {

    @Around("@annotation(retryableTask)")
    public Object around(ProceedingJoinPoint joinPoint, RetryableTask retryableTask) throws Throwable {
        int maxAttempts = Math.max(1, retryableTask.maxAttempts());
        long delayMillis = Math.max(0, retryableTask.delayMillis());
        Throwable last = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return joinPoint.proceed();
            } catch (Throwable ex) {
                last = ex;
                if (attempt >= maxAttempts
                        || !ThrowableMatcher.matches(ex, retryableTask.include(), retryableTask.exclude())) {
                    throw ex;
                }
                sleep(delayMillis);
                delayMillis = Math.round(delayMillis * Math.max(1.0, retryableTask.multiplier()));
            }
        }
        throw last;
    }

    private void sleep(long delayMillis) {
        if (delayMillis <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMillis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
