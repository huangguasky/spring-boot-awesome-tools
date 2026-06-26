package com.github.huangguasky.awesometools.event;

import java.util.concurrent.Executor;
import org.springframework.context.ApplicationEventPublisher;

public class AsyncEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Executor executor;

    public AsyncEventPublisher(ApplicationEventPublisher applicationEventPublisher, Executor executor) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.executor = executor;
    }

    public void publish(Object event) {
        executor.execute(() -> applicationEventPublisher.publishEvent(event));
    }
}
