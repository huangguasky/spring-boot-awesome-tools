package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.PasswordValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordValid, String> {

    private int min;

    private int max;

    private boolean requireLetter;

    private boolean requireDigit;

    private boolean requireSpecial;

    @Override
    public void initialize(PasswordValid constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
        this.requireLetter = constraintAnnotation.requireLetter();
        this.requireDigit = constraintAnnotation.requireDigit();
        this.requireSpecial = constraintAnnotation.requireSpecial();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (value.length() < min || value.length() > max) {
            return false;
        }
        boolean hasLetter = value.chars().anyMatch(Character::isLetter);
        boolean hasDigit = value.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = value.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        return (!requireLetter || hasLetter) && (!requireDigit || hasDigit) && (!requireSpecial || hasSpecial);
    }
}
