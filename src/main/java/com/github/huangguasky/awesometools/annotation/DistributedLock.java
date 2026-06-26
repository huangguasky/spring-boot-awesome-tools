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
public @interface DistributedLock {

    /**
     * Lock key expression. It supports the same expression syntax as other awesome-tools key based annotations.
     */
    String key();

    /**
     * Maximum time to wait for acquiring the lock before failing.
     */
    long waitTime() default 3;

    /**
     * Time unit used by waitTime.
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Error message used when the lock cannot be acquired within waitTime.
     */
    String message() default "Failed to acquire distributed lock";
}
