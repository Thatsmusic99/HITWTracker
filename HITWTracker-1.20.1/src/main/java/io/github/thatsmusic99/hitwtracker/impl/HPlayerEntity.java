package io.github.thatsmusic99.hitwtracker.impl;

import io.github.thatsmusic99.hitwtracker.api.IPlayer;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class HPlayerEntity implements IPlayer<HGameProfile> {

    private final PlayerEntity entity;

    public HPlayerEntity(final @NotNull PlayerEntity entity) {
        this.entity = entity;
    }

    @Override
    public @NotNull UUID getUUID() {
        return this.entity.getUuid();
    }

    @Override
    public @NotNull HGameProfile getGameProfile() {
        return new HGameProfile(this.entity.getGameProfile());
    }

}