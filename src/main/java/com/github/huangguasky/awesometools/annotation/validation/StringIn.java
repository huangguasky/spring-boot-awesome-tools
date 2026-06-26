package com.github.huangguasky.awesometools.annotation.validation;

import com.github.huangguasky.awesometools.validation.StringInValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = StringInValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StringIn {

    /**
     * Allowed string values.
     */
    String[] value();

    /**
     * Whether string comparison should ignore case.
     */
    boolean ignoreCase() default false;

    /**
     * Validation message used when the value is not in the configured set.
     */
    String message() default "Invalid string value";

    /**
     * Bean Validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Bean Validation payload metadata.
     */
    Class<? extends Payload>[] payload() default {};
}
