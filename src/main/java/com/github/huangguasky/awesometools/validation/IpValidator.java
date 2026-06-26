package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.IpValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class IpValidator implements ConstraintValidator<IpValid, String> {

    private static final Pattern IPV4 = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.|$)){4}$");

    private static final Pattern IPV6 = Pattern.compile("^[0-9A-Fa-f:]+$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return IPV4.matcher(value).matches() || (value.contains(":") && IPV6.matcher(value).matches());
    }
}
