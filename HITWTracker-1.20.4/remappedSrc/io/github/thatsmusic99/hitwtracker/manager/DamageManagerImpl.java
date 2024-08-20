package io.github.thatsmusic99.hitwtracker.manager;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class DamageManagerImpl extends DamageManager<DamageSource, StatusEffect> {

    @Override
    public boolean gameTracker$hasBeenDamagedBy(@NotNull String namespace) {
        return this.damage != null
                && this.damage.getSource() != null
                && Registries.ENTITY_TYPE.get(Identifier.splitOn(namespace, ':')) == this.damage.getSource().getType();
    }

    @Override
    public boolean gameTracker$hasBeenArrowDamagedBy(@NotNull String namespace) {
        return this.damage != null
                && this.damage.getSource() instanceof ArrowEntity
                && this.damage.getAttacker() != null
                && Registries.ENTITY_TYPE.get(Identifier.splitOn(namespace, ':')) == this.damage.getAttacker().getType();
    }

    @Override
    public boolean gameTracker$hadStatusEffect(@NotNull String namespace) {
        return effect != null && Registries.STATUS_EFFECT.get(Identifier.splitOn(namespace, ':')) == effect;
    }

    @Override
    public int gameTracker$getAmplifier(@NotNull String namespace) {
        final var player = MinecraftClient.getInstance().player;

        if (player == null) return 0;

        final var effect = player.getStatusEffect(this.effect);

        return gameTracker$hadStatusEffect(namespace) ?
                (this.effect == null ? 0 : (effect == null ? 0 : effect.getAmplifier())) : 0;
    }

    @Override
    public int gameTracker$getAmplifier(@NotNull StatusEffect effect) {

        final var player = MinecraftClient.getInstance().player;

        if (player == null) return 0;

        final var effectInstance = player.getStatusEffect(effect);

        return effectInstance == null ? 0 : effectInstance.getAmplifier();
    }

    @Override
    public int gameTracker$getBlockYNonFinal() {
        if (MinecraftClient.getInstance().player == null) return 0;
        return MinecraftClient.getInstance().player.getBlockY();
    }

    @Override
    public void gameTracker$checkSandfall() {

        // Store the player entity
        final PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;

        final BlockState bottomPos = player.getSteppingBlockState();
        final BlockState playerPos = player.method_48926().getBlockState(player.getBlockPos());

        if (bottomPos.getBlock() == Blocks.SAND || playerPos.getBlock() == Blocks.SAND) {
            gameTracker$onSandfall();
        }
    }
}
