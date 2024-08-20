package io.github.thatsmusic99.hitwtracker;

import io.github.thatsmusic99.hitwtracker.api.IModCore;
import io.github.thatsmusic99.hitwtracker.commands.DeathCauseCommand;
import io.github.thatsmusic99.hitwtracker.gui.GuiManager;
import io.github.thatsmusic99.hitwtracker.gui.GuiManagerImpl;
import io.github.thatsmusic99.hitwtracker.impl.HGameProfile;
import io.github.thatsmusic99.hitwtracker.impl.HPlayerEntity;
import io.github.thatsmusic99.hitwtracker.manager.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class HITWTracker implements ModInitializer, IModCore<DamageSource, StatusEffect, HGameProfile, HPlayerEntity> {

    public static final @NotNull Executor executorService = Executors.newFixedThreadPool(4);
    private static HITWTracker instance;
    private StatisticManager statsManager;
    private PlayerManager playerManager;
    private GuiManager guiManager;
    private DamageManager<DamageSource, StatusEffect> damageManager;

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        instance = this;
        this.statsManager = new StatisticManager();
        this.playerManager = new PlayerManager();
        this.guiManager = new GuiManagerImpl();
        this.damageManager = new DamageManagerImpl();

        final DeathCauseCommand command = new DeathCauseCommand();

        // ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> command.init(dispatcher)));

        CoreContainer.setInstance(this);
    }

    public static HITWTracker get() {
        return instance;
    }

    public StatisticManager getStatsManager() {
        return statsManager;
    }

    @Override
    public @NotNull IPlayerManager<HGameProfile, HPlayerEntity> getPlayerManager() {
        return this.playerManager;
    }

    @Override
    public @NotNull GuiManager getGuiManager() {
        return this.guiManager;
    }

    @Override
    public @NotNull DamageManager<DamageSource, StatusEffect> getDamageManager() {
        return this.damageManager;
    }
}
