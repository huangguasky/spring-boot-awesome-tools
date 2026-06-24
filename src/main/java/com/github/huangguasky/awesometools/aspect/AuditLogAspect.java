package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.AuditLog;
import com.github.huangguasky.awesometools.audit.AuditLogEvent;
import com.github.huangguasky.awesometools.audit.AuditLogRecord;
import com.github.huangguasky.awesometools.core.ExpressionResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 10)
public class AuditLogAspect {

    private final ApplicationEventPublisher eventPublisher;

    private final ExpressionResolver expressionResolver;

    public AuditLogAspect(ApplicationEventPublisher eventPublisher, ExpressionResolver expressionResolver) {
        this.eventPublisher = eventPublisher;
        this.expressionResolver = expressionResolver;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            if (auditLog.recordSuccess()) {
                publish(joinPoint, auditLog, true, null, System.currentTimeMillis() - start);
            }
            return result;
        } catch (Throwable ex) {
            if (auditLog.recordFailure()) {
                publish(joinPoint, auditLog, false, ex.getMessage(), System.currentTimeMillis() - start);
            }
            throw ex;
        }
    }

    private void publish(ProceedingJoinPoint joinPoint, AuditLog auditLog, boolean success, String error, long cost) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AuditLogRecord record = new AuditLogRecord();
        record.setAction(auditLog.value());
        record.setBizNo(auditLog.bizNo().isBlank() ? "" : expressionResolver.resolve(auditLog.bizNo(), joinPoint));
        record.setClassName(signature.getDeclaringTypeName());
        record.setMethodName(signature.getMethod().getName());
        record.setSuccess(success);
        record.setErrorMessage(error);
        record.setCostMillis(cost);
        eventPublisher.publishEvent(new AuditLogEvent(record));
    }
}
