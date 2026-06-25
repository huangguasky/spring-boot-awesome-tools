package com.github.huangguasky.awesometools.annotation.validation;

import com.github.huangguasky.awesometools.validation.EnumValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = EnumValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnumValid {

    /**
     * Enum type that contains the allowed values.
     */
    Class<? extends Enum<?>> enumClass();

    /**
     * Optional no-argument enum method whose return value is compared instead of the enum name.
     */
    String method() default "";

    /**
     * Whether string comparison should ignore case.
     */
    boolean ignoreCase() default false;

    /**
     * Validation message used when the value is not in the allowed enum set.
     */
    String message() default "Invalid enum value";

    /**
     * Bean Validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Bean Validation payload metadata.
     */
    Class<? extends Payload>[] payload() default {};
}
