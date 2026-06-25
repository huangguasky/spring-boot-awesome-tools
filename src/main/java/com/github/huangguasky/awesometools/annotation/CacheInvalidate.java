package com.github.huangguasky.awesometools.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CacheInvalidate {

    /**
     * Cache key expressions to delete for the intercepted invocation.
     */
    String[] keys();

    /**
     * Whether to delete the cache before invoking the method. By default, deletion happens after successful invocation.
     */
    boolean beforeInvocation() default false;
}
