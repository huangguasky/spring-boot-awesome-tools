package com.github.huangguasky.awesometools.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
class ThrowableMatcherTest {

    @Test
    void matchesIncludedAssignableException() {
        assertTrue(ThrowableMatcher.matches(
                new IllegalArgumentException(),
                new Class[] {RuntimeException.class},
                empty()));
    }

    @Test
    void excludeTakesPrecedenceOverInclude() {
        assertFalse(ThrowableMatcher.matches(
                new IllegalArgumentException(),
                new Class[] {RuntimeException.class},
                new Class[] {IllegalArgumentException.class}));
    }

    @Test
    void returnsFalseWhenNoIncludeMatches() {
        assertFalse(ThrowableMatcher.matches(
                new Error(),
                new Class[] {Exception.class},
                empty()));
    }

    private Class<? extends Throwable>[] empty() {
        return new Class[0];
    }
}
