package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.IdCardValid;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class IdCardValidator implements ConstraintValidator<IdCardValid, String> {

    private static final Pattern ID_CARD = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}"
            + "((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");

    private static final int[] WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    private static final char[] CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        if (!ID_CARD.matcher(value).matches()) {
            return false;
        }
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += (value.charAt(i) - '0') * WEIGHTS[i];
        }
        return Character.toUpperCase(value.charAt(17)) == CHECK_CODES[sum % 11];
    }
}
