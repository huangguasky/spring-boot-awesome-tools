package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.CacheInvalidate;
import com.github.huangguasky.awesometools.annotation.QueryCache;
import com.github.huangguasky.awesometools.cache.CacheService;
import com.github.huangguasky.awesometools.core.ExpressionResolver;
import com.github.huangguasky.awesometools.core.KeyBuilder;
import java.time.Duration;
import java.util.Optional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.ClassUtils;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 40)
public class QueryCacheAspect {

    private final CacheService cacheService;

    private final KeyBuilder keyBuilder;

    private final ExpressionResolver expressionResolver;

    public QueryCacheAspect(CacheService cacheService, KeyBuilder keyBuilder, ExpressionResolver expressionResolver) {
        this.cacheService = cacheService;
        this.keyBuilder = keyBuilder;
        this.expressionResolver = expressionResolver;
    }

    @Around("@annotation(queryCache)")
    public Object aroundQuery(ProceedingJoinPoint joinPoint, QueryCache queryCache) throws Throwable {
        String key = queryCache.key().isBlank()
                ? keyBuilder.methodKey("query-cache", joinPoint)
                : keyBuilder.expressionKey("query-cache", queryCache.key(), joinPoint);
        Class<?> returnType = ClassUtils.resolvePrimitiveIfNecessary(
                ((MethodSignature) joinPoint.getSignature()).getReturnType());
        Optional<?> cached = cacheService.get(key, returnType);
        if (cached.isPresent()) {
            return cached.get();
        }
        if (cacheService.exists(key)) {
            return null;
        }
        Object result = joinPoint.proceed();
        if (result != null || queryCache.cacheNull()) {
            Duration ttl = Duration.ofMillis(queryCache.timeUnit().toMillis(queryCache.expireTime()));
            cacheService.put(key, result, ttl);
        }
        return result;
    }

    @Around("@annotation(cacheInvalidate)")
    public Object aroundInvalidate(ProceedingJoinPoint joinPoint, CacheInvalidate cacheInvalidate) throws Throwable {
        if (cacheInvalidate.beforeInvocation()) {
            invalidate(joinPoint, cacheInvalidate);
        }
        try {
            Object result = joinPoint.proceed();
            if (!cacheInvalidate.beforeInvocation()) {
                invalidate(joinPoint, cacheInvalidate);
            }
            return result;
        } catch (Throwable ex) {
            throw ex;
        }
    }

    private void invalidate(ProceedingJoinPoint joinPoint, CacheInvalidate cacheInvalidate) {
        for (String keyExpression : cacheInvalidate.keys()) {
            cacheService.delete(keyBuilder.normalize("query-cache", expressionResolver.resolve(keyExpression, joinPoint)));
        }
    }
}
