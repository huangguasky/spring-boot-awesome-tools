package com.github.huangguasky.awesometools.annotation;

import com.github.huangguasky.awesometools.sensitive.SensitiveType;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {

    /**
     * Built-in masking strategy. CUSTOM uses prefixKeep, suffixKeep, and mask directly.
     */
    SensitiveType type() default SensitiveType.CUSTOM;

    /**
     * Number of leading characters to keep visible for custom masking.
     */
    int prefixKeep() default 0;

    /**
     * Number of trailing characters to keep visible for custom masking.
     */
    int suffixKeep() default 0;

    /**
     * Replacement text inserted between the visible prefix and suffix.
     */
    String mask() default "****";
}
