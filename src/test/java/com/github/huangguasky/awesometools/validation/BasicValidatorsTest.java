package com.github.huangguasky.awesometools.validation;

import com.github.huangguasky.awesometools.annotation.validation.DateRangeValid;
import com.github.huangguasky.awesometools.annotation.validation.PasswordValid;
import com.github.huangguasky.awesometools.annotation.validation.StringIn;
import java.lang.annotation.Annotation;
import java.time.LocalDate;
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

    @Test
    void urlValidatorRequiresSchemeAndHost() {
        UrlValidator validator = new UrlValidator();

        assertTrue(validator.isValid("https://example.com/path", null));
        assertFalse(validator.isValid("example.com/path", null));
    }

    @Test
    void ipValidatorSupportsIpv4AndBasicIpv6() {
        IpValidator validator = new IpValidator();

        assertTrue(validator.isValid("192.168.1.1", null));
        assertTrue(validator.isValid("2001:db8::1", null));
        assertFalse(validator.isValid("999.1.1.1", null));
    }

    @Test
    void passwordValidatorChecksConfiguredRules() {
        PasswordValidator validator = new PasswordValidator();
        validator.initialize(annotation(PasswordValid.class));

        assertTrue(validator.isValid("abc12345", null));
        assertFalse(validator.isValid("abcdefgh", null));
    }

    @Test
    void stringInValidatorChecksAllowedValues() {
        StringInValidator validator = new StringInValidator();
        validator.initialize(annotation(StringIn.class));

        assertTrue(validator.isValid("A", null));
        assertFalse(validator.isValid("C", null));
    }

    @Test
    void dateRangeValidatorComparesConfiguredFields() {
        DateRangeValidator validator = new DateRangeValidator();
        validator.initialize(annotation(DateRangeValid.class));

        assertTrue(validator.isValid(new DateRange(LocalDate.now(), LocalDate.now().plusDays(1)), null));
        assertFalse(validator.isValid(new DateRange(LocalDate.now().plusDays(1), LocalDate.now()), null));
    }

    @SuppressWarnings("unchecked")
    private <A extends Annotation> A annotation(Class<A> annotationType) {
        if (annotationType == PasswordValid.class) {
            return (A) PasswordSample.class.getDeclaredFields()[0].getAnnotation(PasswordValid.class);
        }
        if (annotationType == StringIn.class) {
            return (A) StringInSample.class.getDeclaredFields()[0].getAnnotation(StringIn.class);
        }
        if (annotationType == DateRangeValid.class) {
            return (A) DateRangeSample.class.getAnnotation(DateRangeValid.class);
        }
        throw new IllegalArgumentException("Unsupported annotation: " + annotationType);
    }

    private static class PasswordSample {

        @PasswordValid
        private String password;
    }

    private static class StringInSample {

        @StringIn({"A", "B"})
        private String value;
    }

    @DateRangeValid(startField = "start", endField = "end")
    private static class DateRangeSample {
    }

    private record DateRange(LocalDate start, LocalDate end) {
    }
}
