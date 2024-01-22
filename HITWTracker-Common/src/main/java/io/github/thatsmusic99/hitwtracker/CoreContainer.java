package io.github.thatsmusic99.hitwtracker;

import io.github.thatsmusic99.hitwtracker.api.IModCore;

public class CoreContainer {

    private static IModCore<?, ?> instance = null;

    public static void setInstance(IModCore<?, ?> core) {
        instance = core;
    }

    public static IModCore<?, ?> get() {
        return instance;
    }
}
