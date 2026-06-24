package com.github.huangguasky.awesometools.sensitive;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

public class SensitiveJacksonCustomizer implements Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder builder) {
        builder.postConfigurer(objectMapper -> {
            AnnotationIntrospector existing = objectMapper.getSerializationConfig().getAnnotationIntrospector();
            objectMapper.setAnnotationIntrospector(
                    AnnotationIntrospector.pair(new SensitiveAnnotationIntrospector(), existing));
        });
    }
}
