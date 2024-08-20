package io.github.thatsmusic99.hitwtracker.manager;

import com.mojang.authlib.GameProfile;
import io.github.thatsmusic99.hitwtracker.impl.HGameProfile;
import io.github.thatsmusic99.hitwtracker.impl.HPlayerEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.UserCache;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class PlayerManager extends UserCache implements IPlayerManager<HGameProfile, HPlayerEntity> {

    public PlayerManager() {
        super(MinecraftClient.getInstance().authenticationService.createProfileRepository(), getFile());
    }

    @Override
    public @Nullable HPlayerEntity getPlayer(@NotNull String name) {
        if (MinecraftClient.getInstance().player == null) return null;
        for (PlayerEntity entity : MinecraftClient.getInstance().player.getWorld().getPlayers()) {
            if (entity.getGameProfile().getName().equals(name)) return new HPlayerEntity(entity);
        }
        return null;
    }

    @Override
    public @Nullable HGameProfile getByUUID(@NotNull UUID uuid) {

        final Optional<GameProfile> profile = super.getByUuid(uuid);
        return profile.map(HGameProfile::new).orElse(null);
    }

    @Override
    public void add(@NotNull HGameProfile profile) {
        super.add(profile.getProfile());
    }

    private static @NotNull File getFile() {

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

        return userCacheFile;
    }

}
