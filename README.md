# spring-boot-awesome-tools

Spring Boot 常用业务注解工具包。引入 starter 后即可直接使用：

- `@DistributedLock` 分布式锁
- `@Idempotent` 幂等
- `@AuditLog` 审计日志
- `@NoRepeatSubmit` 防重复提交
- `@Trace` 链路追踪日志
- `@RateLimit` 限流
- `@RetryableTask` 重试
- `@Fallback` 降级兜底
- `@Sensitive` 字段脱敏
- `@QueryCache` 查询缓存
- `@MobileValid`、`@EmailValid`、`@IdCardValid`、`@EnumValid` 参数校验

## 安装

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

如需跨实例生效，请在业务项目中正常配置 Spring Data Redis 的 `StringRedisTemplate`。starter 检测到 Redis 后会自动使用 Redis；没有 Redis 时会使用本地内存实现，适合本地开发和单实例应用。

## 项目构建

本项目同时支持 Maven 和 Gradle 构建：

```bash
mvn test
gradle test
```

## 快速使用

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

    @DistributedLock(key = "'order:pay:' + #orderId", waitTime = 3, leaseTime = 30)
    @Idempotent(key = "'pay:' + #requestId", expireTime = 10, timeUnit = TimeUnit.MINUTES)
    @AuditLog(value = "支付订单", bizNo = "#orderId")
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

注解里的 `key` 支持 SpEL，可使用方法参数名、`#args`、`#methodName`、`#className`。

## 查询缓存

`@QueryCache` 用于查询方法。Redis 存在时缓存到 Redis；没有 Redis 时缓存到本地内存。

```java
@QueryCache(key = "'user:' + #userId", expireTime = 10, timeUnit = TimeUnit.MINUTES, cacheNull = true)
public UserInfo getUser(Long userId) {
    return userRepository.findUser(userId);
}
```

更新数据后可用 `@CacheInvalidate` 删除缓存：

```java
@CacheInvalidate(keys = {"'user:' + #userId"})
public void updateUser(Long userId, UserUpdateCommand command) {
    // update user
}
```

## 重试和降级

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

## 脱敏

字段或 getter 上标记 `@Sensitive` 后，Jackson 输出 JSON 时会自动脱敏，不改变对象本身。

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

## 参数校验

参数校验注解放在 `com.github.huangguasky.awesometools.annotation.validation` 包下：

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

`@EnumValid` 默认校验枚举 `name()`；如果枚举有 `getCode()`，可以这样使用：

```java
@EnumValid(enumClass = UserStatus.class, method = "getCode")
private Integer status;
```

## 审计日志落库

`@AuditLog` 会发布 `AuditLogEvent`，业务侧监听后自行落库、发 MQ 或输出日志：

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

## 配置

```yaml
awesome-tools:
  enabled: true
  key-prefix: awesome-tools
  trace-header: X-Trace-Id
  trace-filter-enabled: true
```

## 说明

- `@DistributedLock`：同一个 key 同一时间只允许一个线程或实例执行。
- `@Idempotent`：同一个 key 在过期时间内只允许成功执行一次；业务异常时会释放 key。
- `@NoRepeatSubmit`：同一个 key 在间隔时间内拒绝重复提交；未指定 key 时按方法签名和参数生成。
- `@RateLimit`：固定窗口限流，适合接口防刷、验证码、登录尝试等场景。
- `@QueryCache`：查询方法缓存，Redis/内存自动切换。
- `@AuditLog`：记录方法、业务号、成功状态、异常消息和耗时，并通过 Spring 事件交给业务系统处理。
- `@Trace`：自动生成或复用 MDC `traceId`，HTTP 请求会写入响应头。
