package com.github.huangguasky.awesometools.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.github.huangguasky.awesometools.annotation.validation.EnumValid;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EnumValidatorTest {

    @Test
    void validatesEnumName() {
        EnumValidator validator = new EnumValidator();
        validator.initialize(annotation("", false));

        assertTrue(validator.isValid("PAID", null));
        assertFalse(validator.isValid("paid", null));
    }

    @Test
    void validatesEnumNameIgnoringCase() {
        EnumValidator validator = new EnumValidator();
        validator.initialize(annotation("", true));

        assertTrue(validator.isValid("paid", null));
    }

    @Test
    void validatesEnumMethodValue() {
        EnumValidator validator = new EnumValidator();
        validator.initialize(annotation("code", false));

        assertTrue(validator.isValid("P", null));
        assertFalse(validator.isValid("PAID", null));
    }

    private EnumValid annotation(String method, boolean ignoreCase) {
        EnumValid annotation = Mockito.mock(EnumValid.class);
        when(annotation.enumClass()).thenAnswer(ignored -> OrderStatus.class);
        when(annotation.method()).thenReturn(method);
        when(annotation.ignoreCase()).thenReturn(ignoreCase);
        return annotation;
    }

    private enum OrderStatus {
        PAID("P"),
        CANCELLED("C");

        private final String code;

        OrderStatus(String code) {
            this.code = code;
        }

        public String code() {
            return code;
        }
    }
}
