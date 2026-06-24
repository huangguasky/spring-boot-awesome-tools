package com.github.huangguasky.awesometools.sensitive;

import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.github.huangguasky.awesometools.annotation.Sensitive;

public class SensitiveAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    public Object findSerializer(Annotated annotated) {
        Sensitive sensitive = annotated.getAnnotation(Sensitive.class);
        if (sensitive != null) {
            return SensitiveStringSerializer.class;
        }
        return super.findSerializer(annotated);
    }
}
