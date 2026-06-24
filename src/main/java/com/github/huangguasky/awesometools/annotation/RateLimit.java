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

    String key() default "";

    long limit();

    long window() default 1;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    String message() default "Too many requests";
}
