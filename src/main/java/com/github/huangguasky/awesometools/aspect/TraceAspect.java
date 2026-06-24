package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.Trace;
import com.github.huangguasky.awesometools.autoconfigure.AwesomeToolsProperties;
import java.util.Arrays;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class TraceAspect {

    private static final Logger log = LoggerFactory.getLogger(TraceAspect.class);

    private final AwesomeToolsProperties properties;

    public TraceAspect(AwesomeToolsProperties properties) {
        this.properties = properties;
    }

    @Around("@within(com.github.huangguasky.awesometools.annotation.Trace) || @annotation(com.github.huangguasky.awesometools.annotation.Trace)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Trace trace = AnnotationUtils.findAnnotation(signature.getMethod(), Trace.class);
        if (trace == null) {
            trace = AnnotationUtils.findAnnotation(signature.getDeclaringType(), Trace.class);
        }

        String previousTraceId = MDC.get("traceId");
        String traceId = previousTraceId == null || previousTraceId.isBlank() ? UUID.randomUUID().toString() : previousTraceId;
        MDC.put("traceId", traceId);
        MDC.put("traceHeader", properties.getTraceHeader());
        long start = System.currentTimeMillis();
        try {
            if (trace != null && trace.logArgs()) {
                log.info("Trace start {} args={}", signature.toShortString(), Arrays.toString(joinPoint.getArgs()));
            }
            Object result = joinPoint.proceed();
            log.info("Trace success {} cost={}ms", signature.toShortString(), System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn("Trace failure {} cost={}ms error={}", signature.toShortString(), System.currentTimeMillis() - start, ex.getMessage());
            throw ex;
        } finally {
            if (previousTraceId == null) {
                MDC.remove("traceId");
            } else {
                MDC.put("traceId", previousTraceId);
            }
            MDC.remove("traceHeader");
        }
    }
}
