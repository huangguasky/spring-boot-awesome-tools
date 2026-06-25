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
     * Lock lease time. For Redis locks, the key expires automatically after this duration unless renewLease is enabled.
     */
    long leaseTime() default 30;

    /**
     * Time unit used by waitTime and leaseTime.
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Whether to keep renewing the lock lease while the annotated method is running.
     */
    boolean renewLease() default false;

    /**
     * Error message used when the lock cannot be acquired within waitTime.
     */
    String message() default "Failed to acquire distributed lock";
}
