package io.github.thatsmusic99.hitwtracker.util;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HITWExecutor {
    public static final @NotNull Executor executorService = Executors.newFixedThreadPool(4);
}
