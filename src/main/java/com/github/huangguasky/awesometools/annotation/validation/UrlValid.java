package com.github.huangguasky.awesometools.annotation.validation;

import com.github.huangguasky.awesometools.validation.UrlValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = UrlValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface UrlValid {

    /**
     * Validation message used when the value is not a valid URL.
     */
    String message() default "Invalid URL";

    /**
     * Bean Validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Bean Validation payload metadata.
     */
    Class<? extends Payload>[] payload() default {};
}
