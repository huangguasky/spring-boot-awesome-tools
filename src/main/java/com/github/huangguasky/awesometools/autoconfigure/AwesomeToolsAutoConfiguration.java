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
import com.github.huangguasky.awesometools.core.RateLimitService;
import com.github.huangguasky.awesometools.sensitive.SensitiveJacksonCustomizer;
import com.github.huangguasky.awesometools.trace.TraceFilter;
import jakarta.servlet.Filter;
import java.net.URI;
import java.time.Duration;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
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
@EnableConfigurationProperties({AwesomeToolsProperties.class, RedisProperties.class})
@ConditionalOnProperty(prefix = "awesome-tools", name = "enabled", havingValue = "true", matchIfMissing = true)
@SuppressWarnings("deprecation")
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
    @ConditionalOnClass(RedissonClient.class)
    static class RedissonLockConfiguration {

        @Bean(destroyMethod = "shutdown")
        @ConditionalOnBean(type = "org.springframework.data.redis.core.StringRedisTemplate")
        @ConditionalOnMissingBean(RedissonClient.class)
        RedissonClient awesomeToolsRedissonClient(RedisProperties redisProperties) {
            Config config = new Config();
            SingleServerConfig singleServer = config.useSingleServer();
            applyRedisProperties(singleServer, redisProperties);
            return Redisson.create(config);
        }

        @Bean
        @ConditionalOnBean(RedissonClient.class)
        @ConditionalOnMissingBean(LockService.class)
        LockService redissonLockService(RedissonClient redissonClient) {
            return new RedisLockService(redissonClient);
        }

        private void applyRedisProperties(SingleServerConfig singleServer, RedisProperties redisProperties) {
            RedisConnectionDetails details = resolveRedisConnectionDetails(redisProperties);
            singleServer.setAddress(details.address());
            singleServer.setDatabase(details.database());
            if (hasText(details.username())) {
                singleServer.setUsername(details.username());
            }
            if (hasText(details.password())) {
                singleServer.setPassword(details.password());
            }
            if (hasText(redisProperties.getClientName())) {
                singleServer.setClientName(redisProperties.getClientName());
            }
            if (redisProperties.getTimeout() != null) {
                singleServer.setTimeout(toMillis(redisProperties.getTimeout()));
            }
            if (redisProperties.getConnectTimeout() != null) {
                singleServer.setConnectTimeout(toMillis(redisProperties.getConnectTimeout()));
            }
        }

        @SuppressWarnings("deprecation")
        private RedisConnectionDetails resolveRedisConnectionDetails(RedisProperties redisProperties) {
            if (hasText(redisProperties.getUrl())) {
                return fromUrl(redisProperties.getUrl(), redisProperties);
            }
            String scheme = redisProperties.getSsl().isEnabled() ? "rediss" : "redis";
            String address = scheme + "://" + redisProperties.getHost() + ":" + redisProperties.getPort();
            return new RedisConnectionDetails(
                    address,
                    redisProperties.getUsername(),
                    redisProperties.getPassword(),
                    redisProperties.getDatabase());
        }

        private RedisConnectionDetails fromUrl(String url, RedisProperties redisProperties) {
            URI uri = URI.create(url);
            String scheme = uri.getScheme() == null ? "redis" : uri.getScheme();
            int port = uri.getPort() == -1 ? redisProperties.getPort() : uri.getPort();
            String address = scheme + "://" + uri.getHost() + ":" + port;
            String username = redisProperties.getUsername();
            String password = redisProperties.getPassword();
            String userInfo = uri.getUserInfo();
            if (hasText(userInfo)) {
                String[] parts = userInfo.split(":", 2);
                if (parts.length == 2) {
                    username = hasText(parts[0]) ? parts[0] : username;
                    password = parts[1];
                } else {
                    password = userInfo;
                }
            }
            int database = redisProperties.getDatabase();
            String path = uri.getPath();
            if (hasText(path) && path.length() > 1) {
                database = Integer.parseInt(path.substring(1));
            }
            return new RedisConnectionDetails(address, username, password, database);
        }

        private int toMillis(Duration duration) {
            return Math.toIntExact(duration.toMillis());
        }

        private boolean hasText(String value) {
            return value != null && !value.isBlank();
        }

        private record RedisConnectionDetails(String address, String username, String password, int database) {
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(StringRedisTemplate.class)
    static class RedisConfiguration {

        @Bean
        @ConditionalOnBean(StringRedisTemplate.class)
        @ConditionalOnMissingBean(ExpiringStore.class)
        ExpiringStore redisExpiringStore(StringRedisTemplate redisTemplate) {
            return new RedisExpiringStore(redisTemplate);
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
