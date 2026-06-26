package com.github.huangguasky.awesometools.annotation.validation;

import com.github.huangguasky.awesometools.validation.PasswordValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordValid {

    /**
     * Minimum allowed password length.
     */
    int min() default 8;

    /**
     * Maximum allowed password length.
     */
    int max() default 64;

    /**
     * Whether the password must contain at least one letter.
     */
    boolean requireLetter() default true;

    /**
     * Whether the password must contain at least one digit.
     */
    boolean requireDigit() default true;

    /**
     * Whether the password must contain at least one non-letter and non-digit character.
     */
    boolean requireSpecial() default false;

    /**
     * Validation message used when the password does not satisfy the configured rules.
     */
    String message() default "Invalid password";

    /**
     * Bean Validation groups.
     */
    Class<?>[] groups() default {};

    /**
     * Bean Validation payload metadata.
     */
    Class<? extends Payload>[] payload() default {};
}
