package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.EnumValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class EnumValidator implements ConstraintValidator<EnumValid, Object> {

    private final Set<String> values = new HashSet<>();

    private boolean ignoreCase;

    @Override
    public void initialize(EnumValid constraintAnnotation) {
        ignoreCase = constraintAnnotation.ignoreCase();
        for (Enum<?> item : constraintAnnotation.enumClass().getEnumConstants()) {
            Object value = resolveValue(item, constraintAnnotation.method());
            values.add(normalize(String.valueOf(value)));
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        return value == null || values.contains(normalize(String.valueOf(value)));
    }

    private Object resolveValue(Enum<?> item, String methodName) {
        if (methodName == null || methodName.isBlank()) {
            return item.name();
        }
        try {
            Method method = item.getClass().getMethod(methodName);
            return method.invoke(item);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Failed to read enum value by method: " + methodName, ex);
        }
    }

    private String normalize(String value) {
        return ignoreCase ? value.toLowerCase() : value;
    }
}
