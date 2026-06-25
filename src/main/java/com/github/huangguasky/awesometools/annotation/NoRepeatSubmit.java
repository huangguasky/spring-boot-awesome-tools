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
public @interface NoRepeatSubmit {

    /**
     * Submit fingerprint expression. When empty, the aspect builds a key from the method and arguments.
     */
    String key() default "";

    /**
     * Minimum interval during which repeated submits are rejected.
     */
    long interval() default 5;

    /**
     * Time unit used by interval.
     */
    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /**
     * Error message used when a repeated submit is rejected.
     */
    String message() default "Do not submit repeatedly";
}
