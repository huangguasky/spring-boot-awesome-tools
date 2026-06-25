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
public @interface QueryCache {

    /**
     * Cache key expression. When empty, the aspect builds a key from the method and arguments.
     */
    String key() default "";

    /**
     * Cache entry time to live.
     */
    long expireTime() default 5;

    /**
     * Time unit used by expireTime.
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * Whether null query results should be cached.
     */
    boolean cacheNull() default false;
}
