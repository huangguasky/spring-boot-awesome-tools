package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.UrlValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.net.URI;

public class UrlValidator implements ConstraintValidator<UrlValid, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        try {
            URI uri = URI.create(value);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
