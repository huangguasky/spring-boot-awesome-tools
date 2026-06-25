package com.github.huangguasky.awesometools.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fallback {

    /**
     * Fallback method name to invoke when the original method throws a matched exception.
     */
    String method();

    /**
     * Exception types that should trigger fallback handling.
     */
    Class<? extends Throwable>[] include() default {Exception.class};

    /**
     * Exception types that should not trigger fallback handling, even if they match include.
     */
    Class<? extends Throwable>[] exclude() default {};
}
