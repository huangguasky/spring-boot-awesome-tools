package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.MobileValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class MobileValidator implements ConstraintValidator<MobileValid, String> {

    private static final Pattern MOBILE = Pattern.compile("^1[3-9]\\d{9}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || MOBILE.matcher(value).matches();
    }
}
