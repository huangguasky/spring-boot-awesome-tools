package com.github.huangguasky.awesometools.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Trace {

    /**
     * Trace span or operation name. When empty, the aspect uses method context.
     */
    String value() default "";

    /**
     * Whether method arguments should be included in trace logs.
     */
    boolean logArgs() default false;
}
