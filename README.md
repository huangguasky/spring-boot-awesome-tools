# spring-boot-awesome-tools

Annotation-driven utilities for common Spring Boot business concerns. Add the starter and use the annotations directly:

- `@DistributedLock` distributed lock
- `@Idempotent` idempotency guard
- `@AuditLog` audit log event publishing
- `@NoRepeatSubmit` duplicate submit protection
- `@Trace` trace logging
- `@RateLimit` local rate limiting
- `@RetryableTask` retry
- `@Fallback` fallback handling
- `@Sensitive` JSON field masking
- `@QueryCache` query cache
- `@MobileValid`, `@EmailValid`, `@IdCardValid`, `@EnumValid` validation annotations

## Requirements

This project is built with Spring Boot 3.3.5 and targets Java 17. Applications using this starter should run on JDK 17 or later.

## Installation

Maven:

```xml
<dependency>
    <groupId>com.github.huangguasky</groupId>
    <artifactId>spring-boot-awesome-tools-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Gradle:

```gradle
implementation 'com.github.huangguasky:spring-boot-awesome-tools-spring-boot-starter:0.1.0-SNAPSHOT'
```

## Redis And Redisson

For cross-instance behavior, configure Redis in your application.

Distributed locks use Redisson `RLock`. When a `StringRedisTemplate` bean exists and no custom `RedissonClient` is provided, the starter creates a `RedissonClient` from `spring.data.redis.*`. Cache, idempotency, and duplicate-submit storage continue to use Spring Data Redis through `StringRedisTemplate`. Rate limiting is local in-memory only.

Typical single-node Redis configuration:

```yaml
spring:
  data:
    redis:
      host: 127.0.0.1
      port: 6379
      database: 0
      password:
```

For cluster, sentinel, or advanced Redisson settings, provide your own `RedissonClient` bean:

```java
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfiguration {

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        return Redisson.create(config);
    }
}
```

Without the matching Redis beans, the starter falls back to local in-memory implementations where available. This is useful for local development and single-instance applications.

## Build

The project supports Maven and Gradle:

```bash
mvn test
gradle test
```

## Quick Start

```java
import com.github.huangguasky.awesometools.annotation.AuditLog;
import com.github.huangguasky.awesometools.annotation.DistributedLock;
import com.github.huangguasky.awesometools.annotation.Idempotent;
import com.github.huangguasky.awesometools.annotation.NoRepeatSubmit;
import com.github.huangguasky.awesometools.annotation.QueryCache;
import com.github.huangguasky.awesometools.annotation.RateLimit;
import com.github.huangguasky.awesometools.annotation.Trace;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;

@Trace
@Service
public class OrderService {

    @DistributedLock(key = "'order:pay:' + #orderId", waitTime = 3)
    @Idempotent(key = "'pay:' + #requestId", expireTime = 10, timeUnit = TimeUnit.MINUTES)
    @AuditLog(value = "pay order", bizNo = "#orderId")
    public void pay(Long orderId, String requestId) {
        // business code
    }

    @NoRepeatSubmit(interval = 3)
    public void submit(String formId) {
        // business code
    }

    @RateLimit(key = "'order:detail:' + #userId", limit = 20, window = 1)
    @QueryCache(key = "'order:' + #orderId", expireTime = 5, timeUnit = TimeUnit.MINUTES)
    public OrderDetail detail(Long userId, Long orderId) {
        return queryOrder(orderId);
    }
}
```

Annotation `key` values support SpEL. You can use method parameter names, `#args`, `#methodName`, and `#className`.

## Local Rate Limiting

`@RateLimit` supports four local in-memory algorithms. The default is `FIXED_WINDOW`.

```java
import com.github.huangguasky.awesometools.annotation.RateLimit;
import com.github.huangguasky.awesometools.annotation.RateLimitType;
import java.util.concurrent.TimeUnit;

@RateLimit(
        key = "'sms:' + #mobile",
        limit = 3,
        window = 60,
        timeUnit = TimeUnit.SECONDS,
        type = RateLimitType.SLIDING_WINDOW)
public void sendSms(String mobile) {
    // send sms
}
```

- `FIXED_WINDOW`: allows at most `limit` requests in each fixed window.
- `SLIDING_WINDOW`: allows at most `limit` requests in any rolling `window`.
- `LEAKY_BUCKET`: uses a bucket with capacity `limit` and leaks at `limit / window`.
- `TOKEN_BUCKET`: uses a bucket with capacity `limit` and refills at `limit / window`, allowing short bursts.

## Query Cache

`@QueryCache` caches query method results. With Redis, values are stored in Redis. Without Redis, values are stored in local memory.

```java
@QueryCache(key = "'user:' + #userId", expireTime = 10, timeUnit = TimeUnit.MINUTES, cacheNull = true)
public UserInfo getUser(Long userId) {
    return userRepository.findUser(userId);
}
```

After updating data, use `@CacheInvalidate` to delete cache entries:

```java
@CacheInvalidate(keys = {"'user:' + #userId"})
public void updateUser(Long userId, UserUpdateCommand command) {
    // update user
}
```

## Retry And Fallback

```java
@RetryableTask(maxAttempts = 3, delayMillis = 200, multiplier = 2.0)
@Fallback(method = "defaultUser")
public UserInfo getRemoteUser(Long userId) {
    return remoteClient.getUser(userId);
}

private UserInfo defaultUser(Long userId, Throwable ex) {
    return UserInfo.empty(userId);
}
```

## Sensitive Data Masking

Mark fields or getters with `@Sensitive`. Jackson output will be masked without changing the original object.

```java
import com.github.huangguasky.awesometools.annotation.Sensitive;
import com.github.huangguasky.awesometools.sensitive.SensitiveType;

public class UserInfo {

    @Sensitive(type = SensitiveType.PHONE)
    private String phone;

    @Sensitive(type = SensitiveType.EMAIL)
    private String email;
}
```

## Validation

Validation annotations live under `com.github.huangguasky.awesometools.annotation.validation`.

```java
import com.github.huangguasky.awesometools.annotation.validation.EmailValid;
import com.github.huangguasky.awesometools.annotation.validation.EnumValid;
import com.github.huangguasky.awesometools.annotation.validation.IdCardValid;
import com.github.huangguasky.awesometools.annotation.validation.MobileValid;

public class UserCreateRequest {

    @MobileValid
    private String mobile;

    @EmailValid
    private String email;

    @IdCardValid
    private String idCard;

    @EnumValid(enumClass = UserStatus.class)
    private String status;
}
```

`@EnumValid` validates enum `name()` by default. If the enum exposes a code method, configure it like this:

```java
@EnumValid(enumClass = UserStatus.class, method = "getCode")
private Integer status;
```

## Audit Logs

`@AuditLog` publishes `AuditLogEvent`. The starter does not persist audit logs by itself. Applications can listen for the event and save records, send messages, or write logs.

Option 1: `@EventListener`

```java
import com.github.huangguasky.awesometools.audit.AuditLogEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AuditLogListener {

    @EventListener
    public void onAuditLog(AuditLogEvent event) {
        var record = event.getRecord();
        // save record
    }
}
```

Option 2: `ApplicationListener<AuditLogEvent>`

```java
import com.github.huangguasky.awesometools.audit.AuditLogEvent;
import com.github.huangguasky.awesometools.audit.AuditLogRecord;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class AuditLogApplicationListener implements ApplicationListener<AuditLogEvent> {

    @Override
    public void onApplicationEvent(AuditLogEvent event) {
        AuditLogRecord record = event.getRecord();
        // save record, send MQ, or write log
    }
}
```

## Configuration

```yaml
awesome-tools:
  enabled: true
  key-prefix: awesome-tools
  trace-header: X-Trace-Id
  trace-filter-enabled: true
```

## Feature Notes

- `@DistributedLock`: allows only one thread or instance to execute for the same key. With `RedissonClient`, it uses Redisson `RLock` and Redisson watchdog renewal.
- `@Idempotent`: allows one successful execution for the same key within the expiry period. The key is released when business execution fails.
- `@NoRepeatSubmit`: rejects repeated submits for the same key within the configured interval. If no key is specified, the method signature and arguments are used.
- `@RateLimit`: local in-memory rate limiting with fixed window, sliding window, leaky bucket, and token bucket algorithms.
- `@QueryCache`: query method cache with Redis/local-memory switching.
- `@AuditLog`: records method, business number, success status, exception message, and cost, then publishes a Spring event.
- `@Trace`: creates or reuses an MDC `traceId`; HTTP requests also receive the trace id in the response header.
