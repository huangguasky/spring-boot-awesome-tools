package com.github.huangguasky.awesometools.sensitive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.github.huangguasky.awesometools.annotation.Sensitive;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SensitiveMaskerTest {

    @Test
    void keepsBlankValuesUnchanged() {
        assertEquals("", SensitiveMasker.mask("", sensitive(SensitiveType.CUSTOM, 1, 1, "***")));
    }

    @Test
    void masksPhoneWithBuiltInRule() {
        assertEquals("138****8000", SensitiveMasker.mask("13800138000", sensitive(SensitiveType.PHONE, 0, 0, "****")));
    }

    @Test
    void masksEmailWithBuiltInRule() {
        assertEquals("a****@example.com", SensitiveMasker.mask("alice@example.com", sensitive(SensitiveType.EMAIL, 0, 0, "****")));
    }

    @Test
    void masksCustomValueUsingConfiguredVisibleCharacters() {
        assertEquals("ab***yz", SensitiveMasker.mask("abcdefyz", sensitive(SensitiveType.CUSTOM, 2, 2, "***")));
    }

    @Test
    void returnsMaskWhenValueIsShorterThanVisibleCharacters() {
        assertEquals("***", SensitiveMasker.mask("abc", sensitive(SensitiveType.CUSTOM, 2, 2, "***")));
    }

    private Sensitive sensitive(SensitiveType type, int prefixKeep, int suffixKeep, String mask) {
        Sensitive sensitive = Mockito.mock(Sensitive.class);
        when(sensitive.type()).thenReturn(type);
        when(sensitive.prefixKeep()).thenReturn(prefixKeep);
        when(sensitive.suffixKeep()).thenReturn(suffixKeep);
        when(sensitive.mask()).thenReturn(mask);
        return sensitive;
    }
}
