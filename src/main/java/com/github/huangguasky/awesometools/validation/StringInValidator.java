package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.StringIn;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class StringInValidator implements ConstraintValidator<StringIn, String> {

    private final Set<String> values = new HashSet<>();

    private boolean ignoreCase;

    @Override
    public void initialize(StringIn constraintAnnotation) {
        this.ignoreCase = constraintAnnotation.ignoreCase();
        for (String value : constraintAnnotation.value()) {
            values.add(normalize(value));
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || values.contains(normalize(value));
    }

    private String normalize(String value) {
        return ignoreCase && value != null ? value.toLowerCase() : value;
    }
}
