package io.github.thatsmusic99.hitwtracker;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.github.thatsmusic99.hitwtracker.api.IModCore;
import io.github.thatsmusic99.hitwtracker.commands.DeathCauseCommand;
import io.github.thatsmusic99.hitwtracker.commands.TrapSuggestionProvider;
import io.github.thatsmusic99.hitwtracker.gui.GuiManager;
import io.github.thatsmusic99.hitwtracker.gui.GuiManagerImpl;
import io.github.thatsmusic99.hitwtracker.impl.HGameProfile;
import io.github.thatsmusic99.hitwtracker.impl.HPlayerEntity;
import io.github.thatsmusic99.hitwtracker.manager.*;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Function;

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

        // ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) -> ));

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
