package io.github.thatsmusic99.hitwtracker.manager;

import io.github.thatsmusic99.hitwtracker.api.IGameProfile;
import io.github.thatsmusic99.hitwtracker.api.IPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface IPlayerManager<G extends IGameProfile, P extends IPlayer<G>> {

    @Nullable P getPlayer(final @NotNull String name);

    @Nullable G getByUUID(final @NotNull UUID uuid);

    void add(final @NotNull G gameProfile);
}
