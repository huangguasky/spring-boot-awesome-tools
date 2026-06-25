package com.github.huangguasky.awesometools.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BasicValidatorsTest {

    @Test
    void emailValidatorAllowsBlankAndValidEmailOnly() {
        EmailValidator validator = new EmailValidator();

        assertTrue(validator.isValid(null, null));
        assertTrue(validator.isValid("alice@example.com", null));
        assertFalse(validator.isValid("alice@", null));
    }

    @Test
    void mobileValidatorAllowsBlankAndChineseMobileOnly() {
        MobileValidator validator = new MobileValidator();

        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("13800138000", null));
        assertFalse(validator.isValid("12800138000", null));
    }

    @Test
    void idCardValidatorChecksFormatAndChecksum() {
        IdCardValidator validator = new IdCardValidator();

        assertTrue(validator.isValid("11010519491231002X", null));
        assertFalse(validator.isValid("110105194912310021", null));
        assertFalse(validator.isValid("not-id-card", null));
    }
}
