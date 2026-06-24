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

    SensitiveType type() default SensitiveType.CUSTOM;

    int prefixKeep() default 0;

    int suffixKeep() default 0;

    String mask() default "****";
}
