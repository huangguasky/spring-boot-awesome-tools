package com.github.huangguasky.awesometools.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryableTask {

    /**
     * Maximum number of invocation attempts, including the initial attempt.
     */
    int maxAttempts() default 3;

    /**
     * Initial delay in milliseconds before retrying after a matched failure.
     */
    long delayMillis() default 200;

    /**
     * Multiplier applied to the delay after each failed attempt.
     */
    double multiplier() default 1.0;

    /**
     * Exception types that should trigger a retry.
     */
    Class<? extends Throwable>[] include() default {Exception.class};

    /**
     * Exception types that should not trigger a retry, even if they match include.
     */
    Class<? extends Throwable>[] exclude() default {};
}
