package com.github.huangguasky.awesometools.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface SlowLog {

    /**
     * Business operation name written to slow-call logs. When empty, the aspect uses the method signature.
     */
    String value() default "";

    /**
     * Minimum execution time in milliseconds required before a warning log is emitted.
     */
    long thresholdMillis() default 500;

    /**
     * Whether method arguments should be included in the slow-call warning log.
     */
    boolean logArgs() default false;
}
