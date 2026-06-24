package com.github.huangguasky.awesometools.sensitive;

import com.github.huangguasky.awesometools.annotation.Sensitive;

public final class SensitiveMasker {

    private SensitiveMasker() {
    }

    public static String mask(String value, Sensitive sensitive) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return switch (sensitive.type()) {
            case PHONE -> keep(value, 3, 4, sensitive.mask());
            case EMAIL -> maskEmail(value, sensitive.mask());
            case ID_CARD -> keep(value, 4, 4, sensitive.mask());
            case BANK_CARD -> keep(value, 4, 4, sensitive.mask());
            case NAME -> keep(value, 1, 0, sensitive.mask());
            case ADDRESS -> keep(value, 6, 0, sensitive.mask());
            case CUSTOM -> keep(value, sensitive.prefixKeep(), sensitive.suffixKeep(), sensitive.mask());
        };
    }

    private static String keep(String value, int prefixKeep, int suffixKeep, String mask) {
        if (value.length() <= prefixKeep + suffixKeep) {
            return mask;
        }
        String prefix = prefixKeep <= 0 ? "" : value.substring(0, prefixKeep);
        String suffix = suffixKeep <= 0 ? "" : value.substring(value.length() - suffixKeep);
        return prefix + mask + suffix;
    }

    private static String maskEmail(String value, String mask) {
        int at = value.indexOf('@');
        if (at <= 1) {
            return mask;
        }
        return value.charAt(0) + mask + value.substring(at);
    }
}
