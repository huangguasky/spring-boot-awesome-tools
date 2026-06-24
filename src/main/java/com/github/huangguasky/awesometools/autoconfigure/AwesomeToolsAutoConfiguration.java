package com.github.huangguasky.awesometools.autoconfigure;

import com.github.huangguasky.awesometools.aspect.AuditLogAspect;
import com.github.huangguasky.awesometools.aspect.DistributedLockAspect;
import com.github.huangguasky.awesometools.aspect.IdempotentAspect;
import com.github.huangguasky.awesometools.aspect.NoRepeatSubmitAspect;
import com.github.huangguasky.awesometools.aspect.TraceAspect;
import com.github.huangguasky.awesometools.core.ExpiringStore;
import com.github.huangguasky.awesometools.core.ExpressionResolver;
import com.github.huangguasky.awesometools.core.InMemoryExpiringStore;
import com.github.huangguasky.awesometools.core.InMemoryLockService;
import com.github.huangguasky.awesometools.core.KeyBuilder;
import com.github.huangguasky.awesometools.core.LockService;
import com.github.huangguasky.awesometools.core.RedisExpiringStore;
import com.github.huangguasky.awesometools.core.RedisLockService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
    }
}
