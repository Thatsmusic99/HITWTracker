package io.github.thatsmusic99.hitwtracker;

import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HITWTracker implements ModInitializer {

    public static final @NotNull Executor executorService = Executors.newFixedThreadPool(4);
    private static HITWTracker instance;
    private StatisticManager statsManager;

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        instance = this;
        this.statsManager = new StatisticManager();
    }

    public static HITWTracker get() {
        return instance;
    }

    public StatisticManager getStatsManager() {
        return statsManager;
    }
}
