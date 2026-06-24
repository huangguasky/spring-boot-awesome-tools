package com.github.huangguasky.awesometools.sensitive;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.github.huangguasky.awesometools.annotation.Sensitive;
import java.io.IOException;

public class SensitiveStringSerializer extends JsonSerializer<Object> implements ContextualSerializer {

    private final Sensitive sensitive;

    public SensitiveStringSerializer() {
        this(null);
    }

    public SensitiveStringSerializer(Sensitive sensitive) {
        this.sensitive = sensitive;
    }

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (sensitive == null) {
            gen.writeString(String.valueOf(value));
            return;
        }
        gen.writeString(SensitiveMasker.mask(String.valueOf(value), sensitive));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        if (property == null) {
            return this;
        }
        Sensitive annotation = property.getAnnotation(Sensitive.class);
        if (annotation == null) {
            annotation = property.getContextAnnotation(Sensitive.class);
        }
        return annotation == null ? this : new SensitiveStringSerializer(annotation);
    }
}
