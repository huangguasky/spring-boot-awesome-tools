package com.github.huangguasky.awesometools.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Rate limit key expression. When empty, the aspect builds a key from the method and arguments.
     */
    String key() default "";

    /**
     * Maximum number of allowed calls within the configured window.
     */
    long limit();

    /**
     * Length of the rate limit window.
     */
    long window() default 1;

    /**
     * Time unit used by window.
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Error message used when the limit is exceeded.
     */
    String message() default "Too many requests";
}
