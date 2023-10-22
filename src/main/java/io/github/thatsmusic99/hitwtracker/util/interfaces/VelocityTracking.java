package io.github.thatsmusic99.hitwtracker.util.interfaces;

import org.jetbrains.annotations.Nullable;

public interface VelocityTracking {

    void onExplosion();

    void onRodPull();

    void onSandfall();

    void onBlastOff();

    void onWeb();

    void onGround(boolean onGround);

    @Nullable VelocityStatus getVelocityStatus();

    enum VelocityStatus {
        FISHING_RODS,
        HOT_POTATO,
        SANDFALL,
        COBWEBS,
        STICKY_SHOES,
        BLAST_OFF
    }
}
