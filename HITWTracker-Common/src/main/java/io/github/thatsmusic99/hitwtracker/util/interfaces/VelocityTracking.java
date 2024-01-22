package io.github.thatsmusic99.hitwtracker.util.interfaces;

import org.jetbrains.annotations.Nullable;

public interface VelocityTracking {

    void gameTracker$onExplosion();

    void gameTracker$onRodPull();

    void gameTracker$onSandfall();

    void gameTracker$onBlastOff();

    void gameTracker$onWeb();

    void gameTracker$onGround(boolean onGround);

    int gameTracker$getBlockYNonFinal();

    @Nullable VelocityStatus gameTracker$getVelocityStatus();

    enum VelocityStatus {
        FISHING_RODS,
        HOT_POTATO,
        SANDFALL,
        COBWEBS,
        STICKY_SHOES,
        BLAST_OFF
    }
}
