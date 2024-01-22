package io.github.thatsmusic99.hitwtracker;

import io.github.thatsmusic99.hitwtracker.api.IModCore;
import io.github.thatsmusic99.hitwtracker.gui.GuiManager;
import io.github.thatsmusic99.hitwtracker.gui.GuiManagerImpl;
import io.github.thatsmusic99.hitwtracker.impl.HGameProfile;
import io.github.thatsmusic99.hitwtracker.impl.HPlayerEntity;
import io.github.thatsmusic99.hitwtracker.manager.IPlayerManager;
import io.github.thatsmusic99.hitwtracker.manager.PlayerManager;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import net.fabricmc.api.ModInitializer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HITWTracker implements ModInitializer, IModCore<HGameProfile, HPlayerEntity> {

    public static final @NotNull Executor executorService = Executors.newFixedThreadPool(4);
    private static HITWTracker instance;
    private StatisticManager statsManager;
    private PlayerManager playerManager;
    private GuiManager guiManager;

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        instance = this;
        this.statsManager = new StatisticManager();
        this.playerManager = new PlayerManager();
        this.guiManager = new GuiManagerImpl();

        CoreContainer.setInstance(this);
    }

    public static HITWTracker get() {
        return instance;
    }

    @Override
    public @NotNull IPlayerManager<HGameProfile, HPlayerEntity> getPlayerManager() {
        return this.playerManager;
    }

    @Override
    public @NotNull GuiManager getGuiManager() {
        return this.guiManager;
    }
}
