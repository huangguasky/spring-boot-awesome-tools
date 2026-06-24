package com.github.huangguasky.awesometools.annotation.validation;

import com.github.huangguasky.awesometools.validation.IdCardValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = IdCardValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface IdCardValid {

    String message() default "Invalid id card number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
