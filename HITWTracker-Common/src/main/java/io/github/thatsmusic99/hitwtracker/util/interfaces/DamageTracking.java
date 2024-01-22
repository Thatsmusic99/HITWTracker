package io.github.thatsmusic99.hitwtracker.util.interfaces;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DamageTracking<T> {

    @Nullable T gameTracker$getLastDamageSource();

    boolean gameTracker$hasBeenDamagedBy(final @NotNull String namespace);

    boolean gameTracker$hasBeenArrowDamagedBy(final @NotNull String namespace);

    void gameTracker$flush();

    void gameTracker$onDamage(T damageSource);
}
