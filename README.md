# spring-boot-awesome-tools

Spring Boot 常用业务注解工具包。引入 starter 后即可直接使用：

- `@DistributedLock` 分布式锁
- `@Idempotent` 幂等
- `@AuditLog` 审计日志
- `@NoRepeatSubmit` 防重复提交
- `@Trace` 链路追踪日志

## 安装

```xml
<dependency>
    <groupId>com.github.huangguasky</groupId>
    <artifactId>spring-boot-awesome-tools-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

如需跨实例生效，请在业务项目中正常配置 Spring Data Redis 的 `StringRedisTemplate`。starter 检测到 Redis 后会自动使用 Redis；没有 Redis 时会使用本地内存实现，适合本地开发和单实例应用。

## 快速使用

```java
import com.github.huangguasky.awesometools.annotation.AuditLog;
import com.github.huangguasky.awesometools.annotation.DistributedLock;
import com.github.huangguasky.awesometools.annotation.Idempotent;
import com.github.huangguasky.awesometools.annotation.NoRepeatSubmit;
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
}
```

注解里的 `key` 支持 SpEL，可使用方法参数名、`#args`、`#methodName`、`#className`。

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
```

## 说明

- `@DistributedLock`：同一个 key 同一时间只允许一个线程或实例执行。
- `@Idempotent`：同一个 key 在过期时间内只允许成功执行一次；业务异常时会释放 key。
- `@NoRepeatSubmit`：同一个 key 在间隔时间内拒绝重复提交；未指定 key 时按方法签名和参数生成。
- `@AuditLog`：记录方法、业务号、成功状态、异常消息和耗时，并通过 Spring 事件交给业务系统处理。
- `@Trace`：自动生成或复用 MDC `traceId`，记录方法执行耗时。
