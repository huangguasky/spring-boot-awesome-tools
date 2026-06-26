package com.github.huangguasky.awesometools.annotation.validation;

import com.github.huangguasky.awesometools.validation.DateRangeValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DateRangeValid {

    /**
     * Name of the start date/time property on the annotated object.
     */
    String startField();

    /**
     * Name of the end date/time property on the annotated object.
     */
    String endField();

    /**
     * Whether the start value may be equal to the end value.
     */
    boolean allowEqual() default true;

    /**
     * Validation message used when the configured date range is invalid.
     */
    String message() default "Invalid date range";

    /**
     * Bean Validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Bean Validation payload metadata.
     */
    Class<? extends Payload>[] payload() default {};
}
