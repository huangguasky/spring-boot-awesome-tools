package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.RateLimit;
import com.github.huangguasky.awesometools.core.KeyBuilder;
import com.github.huangguasky.awesometools.core.RateLimitService;
import com.github.huangguasky.awesometools.exception.AwesomeToolsException;
import java.time.Duration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    private final KeyBuilder keyBuilder;

    public RateLimitAspect(RateLimitService rateLimitService, KeyBuilder keyBuilder) {
        this.rateLimitService = rateLimitService;
        this.keyBuilder = keyBuilder;
    }

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = rateLimit.key().isBlank()
                ? keyBuilder.methodKey("rate-limit", joinPoint)
                : keyBuilder.expressionKey("rate-limit", rateLimit.key(), joinPoint);
        Duration window = Duration.ofMillis(rateLimit.timeUnit().toMillis(rateLimit.window()));
        if (!rateLimitService.tryAcquire(key, rateLimit.limit(), window)) {
            throw new AwesomeToolsException(rateLimit.message());
        }
        return joinPoint.proceed();
    }
}
