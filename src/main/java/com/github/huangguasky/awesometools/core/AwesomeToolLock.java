package com.github.huangguasky.awesometools.core;

public interface AwesomeToolLock extends AutoCloseable {

    @Override
    void close();
}
