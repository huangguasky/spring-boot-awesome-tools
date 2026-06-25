package com.github.huangguasky.awesometools.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    /**
     * Audit action name or description. When empty, the aspect may derive context from the intercepted method.
     */
    String value() default "";

    /**
     * Business identifier expression, such as an order number or user id, used to correlate audit records.
     */
    String bizNo() default "";

    /**
     * Whether to publish an audit record when the method completes successfully.
     */
    boolean recordSuccess() default true;

    /**
     * Whether to publish an audit record when the method throws an exception.
     */
    boolean recordFailure() default true;
}
