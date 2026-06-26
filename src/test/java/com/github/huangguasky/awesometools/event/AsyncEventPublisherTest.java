package com.github.huangguasky.awesometools.event;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncEventPublisherTest {

    @Test
    void publishesEventUsingExecutor() {
        AtomicReference<Object> published = new AtomicReference<>();
        ApplicationEventPublisher publisher = published::set;
        AsyncEventPublisher asyncEventPublisher = new AsyncEventPublisher(publisher, Runnable::run);

        asyncEventPublisher.publish("hello");

        assertThat(published.get()).isEqualTo("hello");
    }
}
