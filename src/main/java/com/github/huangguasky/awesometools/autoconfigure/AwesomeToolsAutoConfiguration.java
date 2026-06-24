package com.github.huangguasky.awesometools.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.huangguasky.awesometools.aspect.AuditLogAspect;
import com.github.huangguasky.awesometools.aspect.DistributedLockAspect;
import com.github.huangguasky.awesometools.aspect.FallbackAspect;
import com.github.huangguasky.awesometools.aspect.IdempotentAspect;
import com.github.huangguasky.awesometools.aspect.NoRepeatSubmitAspect;
import com.github.huangguasky.awesometools.aspect.QueryCacheAspect;
import com.github.huangguasky.awesometools.aspect.RateLimitAspect;
import com.github.huangguasky.awesometools.aspect.RetryableTaskAspect;
import com.github.huangguasky.awesometools.aspect.TraceAspect;
import com.github.huangguasky.awesometools.cache.CacheService;
import com.github.huangguasky.awesometools.cache.InMemoryCacheService;
import com.github.huangguasky.awesometools.cache.RedisCacheService;
import com.github.huangguasky.awesometools.core.ExpiringStore;
import com.github.huangguasky.awesometools.core.ExpressionResolver;
import com.github.huangguasky.awesometools.core.InMemoryExpiringStore;
import com.github.huangguasky.awesometools.core.InMemoryLockService;
import com.github.huangguasky.awesometools.core.InMemoryRateLimitService;
import com.github.huangguasky.awesometools.core.KeyBuilder;
import com.github.huangguasky.awesometools.core.LockService;
import com.github.huangguasky.awesometools.core.RedisExpiringStore;
import com.github.huangguasky.awesometools.core.RedisLockService;
import com.github.huangguasky.awesometools.core.RedisRateLimitService;
import com.github.huangguasky.awesometools.core.RateLimitService;
import com.github.huangguasky.awesometools.sensitive.SensitiveJacksonCustomizer;
import com.github.huangguasky.awesometools.trace.TraceFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@AutoConfiguration
@EnableConfigurationProperties(AwesomeToolsProperties.class)
@ConditionalOnProperty(prefix = "awesome-tools", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AwesomeToolsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExpressionResolver expressionResolver() {
        return new ExpressionResolver();
    }

    @Bean
    @ConditionalOnMissingBean
    public KeyBuilder keyBuilder(AwesomeToolsProperties properties, ExpressionResolver expressionResolver) {
        return new KeyBuilder(properties, expressionResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public DistributedLockAspect distributedLockAspect(LockService lockService, KeyBuilder keyBuilder) {
        return new DistributedLockAspect(lockService, keyBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdempotentAspect idempotentAspect(ExpiringStore expiringStore, KeyBuilder keyBuilder) {
        return new IdempotentAspect(expiringStore, keyBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public RateLimitAspect rateLimitAspect(RateLimitService rateLimitService, KeyBuilder keyBuilder) {
        return new RateLimitAspect(rateLimitService, keyBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public NoRepeatSubmitAspect noRepeatSubmitAspect(ExpiringStore expiringStore, KeyBuilder keyBuilder) {
        return new NoRepeatSubmitAspect(expiringStore, keyBuilder);
    }

    @Bean
    @ConditionalOnMissingBean
    public AuditLogAspect auditLogAspect(ApplicationEventPublisher eventPublisher, ExpressionResolver expressionResolver) {
        return new AuditLogAspect(eventPublisher, expressionResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceAspect traceAspect(AwesomeToolsProperties properties) {
        return new TraceAspect(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RetryableTaskAspect retryableTaskAspect() {
        return new RetryableTaskAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public FallbackAspect fallbackAspect() {
        return new FallbackAspect();
    }

    @Bean
    @ConditionalOnMissingBean
    public QueryCacheAspect queryCacheAspect(
            CacheService cacheService, KeyBuilder keyBuilder, ExpressionResolver expressionResolver) {
        return new QueryCacheAspect(cacheService, keyBuilder, expressionResolver);
    }

    @Bean
    @ConditionalOnClass(ObjectMapper.class)
    @ConditionalOnMissingBean
    public SensitiveJacksonCustomizer sensitiveJacksonCustomizer() {
        return new SensitiveJacksonCustomizer();
    }

    @Bean
    @ConditionalOnClass(Filter.class)
    @ConditionalOnProperty(prefix = "awesome-tools", name = "trace-filter-enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean(name = "awesomeToolsTraceFilterRegistration")
    public FilterRegistrationBean<TraceFilter> awesomeToolsTraceFilterRegistration(AwesomeToolsProperties properties) {
        FilterRegistrationBean<TraceFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new TraceFilter(properties));
        registration.setName("awesomeToolsTraceFilter");
        registration.setOrder(Integer.MIN_VALUE + 100);
        return registration;
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    static class RedisConfiguration {

        @Bean
        @ConditionalOnBean(StringRedisTemplate.class)
        @ConditionalOnMissingBean(LockService.class)
        LockService redisLockService(StringRedisTemplate redisTemplate) {
            return new RedisLockService(redisTemplate);
        }

        @Bean
        @ConditionalOnBean(StringRedisTemplate.class)
        @ConditionalOnMissingBean(ExpiringStore.class)
        ExpiringStore redisExpiringStore(StringRedisTemplate redisTemplate) {
            return new RedisExpiringStore(redisTemplate);
        }

        @Bean
        @ConditionalOnBean(StringRedisTemplate.class)
        @ConditionalOnMissingBean(RateLimitService.class)
        RateLimitService redisRateLimitService(StringRedisTemplate redisTemplate) {
            return new RedisRateLimitService(redisTemplate);
        }

        @Bean
        @ConditionalOnBean({StringRedisTemplate.class, ObjectMapper.class})
        @ConditionalOnMissingBean(CacheService.class)
        CacheService redisCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
            return new RedisCacheService(redisTemplate, objectMapper);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class FallbackConfiguration {

        @Bean
        @ConditionalOnMissingBean
        LockService inMemoryLockService() {
            return new InMemoryLockService();
        }

        @Bean
        @ConditionalOnMissingBean
        ExpiringStore inMemoryExpiringStore() {
            return new InMemoryExpiringStore();
        }

        @Bean
        @ConditionalOnMissingBean
        RateLimitService inMemoryRateLimitService() {
            return new InMemoryRateLimitService();
        }

        @Bean
        @ConditionalOnMissingBean
        CacheService inMemoryCacheService() {
            return new InMemoryCacheService();
        }
    }
}
