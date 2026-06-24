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

    int maxAttempts() default 3;

    long delayMillis() default 200;

    double multiplier() default 1.0;

    Class<? extends Throwable>[] include() default {Exception.class};

    Class<? extends Throwable>[] exclude() default {};
}
