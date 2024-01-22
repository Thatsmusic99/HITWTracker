package io.github.thatsmusic99.hitwtracker.util.interfaces;

public interface HotPotatoTracking {

    void gameTracker$onPotatoWarning();

    void gameTracker$coolPotatoWarning();

    boolean gameTracker$hasPotatoWarning();

    void gameTracker$onExplosionSound();

    void gameTracker$onExplosionLaunch();
}
