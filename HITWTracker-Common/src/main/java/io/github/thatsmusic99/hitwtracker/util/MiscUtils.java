package io.github.thatsmusic99.hitwtracker.util;

import io.github.thatsmusic99.hitwtracker.CoreContainer;
import io.github.thatsmusic99.hitwtracker.api.IGameProfile;
import io.github.thatsmusic99.hitwtracker.manager.IPlayerManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MiscUtils {

    @Contract("null -> null")
    public static String capitalise(@Nullable String str) {
        if (str == null) return null;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    @Contract(pure = true)
    public static String toTime(int seconds) {
        return (seconds / 60) + ":" + (seconds % 60 < 10 ? "0" + (seconds % 60) : (seconds % 60));
    }

    @Contract(pure = true)
    public static <G extends IGameProfile> @NotNull String getUsername(final @Nullable String player) {
        if (player == null) return "N/A";
        final UUID uuid;
        try {
            uuid = UUID.fromString(player);
        } catch (IllegalArgumentException ex) {
            return player;
        }

        final IPlayerManager<G, ?> manager = (IPlayerManager<G, ?>) CoreContainer.get().getPlayerManager();

        G profile = manager.getByUUID(uuid);
        if (profile == null) return player;

        return profile.getName();
    }
}
