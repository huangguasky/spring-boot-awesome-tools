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
public @interface Idempotent {

    /**
     * Idempotency key expression used to identify duplicate requests.
     */
    String key();

    /**
     * Time that the idempotency key remains reserved.
     */
    long expireTime() default 60;

    /**
     * Time unit used by expireTime.
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Error message used when a duplicate request is rejected.
     */
    String message() default "Duplicate request";
}
