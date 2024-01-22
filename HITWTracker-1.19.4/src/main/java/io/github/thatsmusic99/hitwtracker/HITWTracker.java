package io.github.thatsmusic99.hitwtracker;

import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HITWTracker implements ModInitializer {

    public static final @NotNull Executor executorService = Executors.newFixedThreadPool(4);
    private static HITWTracker instance;
    private StatisticManager statsManager;
    private UserCache userCache;

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        instance = this;
        this.statsManager = new StatisticManager();

        final File cache = new File("cache");
        if (!cache.exists()) {
            cache.mkdir();
        }

        final File userCacheFile = new File(cache, "user-cache.json");
        if (!userCacheFile.exists()) {
            try {
                userCacheFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.userCache = new UserCache(MinecraftClient.getInstance().authenticationService.createProfileRepository(),
                userCacheFile);
    }

    public static HITWTracker get() {
        return instance;
    }

    public StatisticManager getStatsManager() {
        return statsManager;
    }

    public UserCache getUserCache() {
        return userCache;
    }
}
