package com.github.huangguasky.awesometools.aspect;

import com.github.huangguasky.awesometools.annotation.SlowLog;
import java.util.Arrays;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;

@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 45)
public class SlowLogAspect {

    @Around("@within(com.github.huangguasky.awesometools.annotation.SlowLog) || @annotation(com.github.huangguasky.awesometools.annotation.SlowLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        SlowLog slowLog = AnnotationUtils.findAnnotation(signature.getMethod(), SlowLog.class);
        if (slowLog == null) {
            slowLog = AnnotationUtils.findAnnotation(signature.getDeclaringType(), SlowLog.class);
        }
        if (slowLog == null) {
            return joinPoint.proceed();
        }

        long start = System.currentTimeMillis();
        try {
            return joinPoint.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost >= slowLog.thresholdMillis()) {
                Logger logger = LoggerFactory.getLogger(signature.getDeclaringType());
                String operation = slowLog.value().isBlank() ? signature.toShortString() : slowLog.value();
                if (slowLog.logArgs()) {
                    logger.warn("Slow call {} cost={}ms args={}", operation, cost, Arrays.toString(joinPoint.getArgs()));
                } else {
                    logger.warn("Slow call {} cost={}ms", operation, cost);
                }
            }
        }
    }
}
