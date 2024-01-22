package io.github.thatsmusic99.hitwtracker.util.interfaces;

import org.jetbrains.annotations.NotNull;

public interface StatusTracking<T> {

    boolean gameTracker$hadStatusEffect(final @NotNull String namespace);

    void gameTracker$applyStatusEffect(final @NotNull T effect);

    int gameTracker$getAmplifier(final @NotNull String namespace);
}
