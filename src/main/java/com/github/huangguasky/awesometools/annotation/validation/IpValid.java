package com.github.huangguasky.awesometools.annotation.validation;

import com.github.huangguasky.awesometools.validation.IpValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IpValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IpValid {

    /**
     * Validation message used when the value is not a valid IP address.
     */
    String message() default "Invalid IP address";

    /**
     * Bean Validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Bean Validation payload metadata.
     */
    Class<? extends Payload>[] payload() default {};
}
