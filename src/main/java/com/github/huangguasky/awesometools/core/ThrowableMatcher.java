package com.github.huangguasky.awesometools.core;

public final class ThrowableMatcher {

    private ThrowableMatcher() {
    }

    public static boolean matches(Throwable throwable, Class<? extends Throwable>[] include, Class<? extends Throwable>[] exclude) {
        for (Class<? extends Throwable> excluded : exclude) {
            if (excluded.isAssignableFrom(throwable.getClass())) {
                return false;
            }
        }
        for (Class<? extends Throwable> included : include) {
            if (included.isAssignableFrom(throwable.getClass())) {
                return true;
            }
        }
        return false;
    }
}
