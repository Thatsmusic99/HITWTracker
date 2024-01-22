package io.github.thatsmusic99.hitwtracker.api;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface IPlayer<G extends IGameProfile> {

    @NotNull UUID getUUID();

    @NotNull G getGameProfile();
}
