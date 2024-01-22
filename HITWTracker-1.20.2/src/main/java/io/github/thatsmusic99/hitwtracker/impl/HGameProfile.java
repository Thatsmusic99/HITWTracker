package io.github.thatsmusic99.hitwtracker.impl;

import com.mojang.authlib.GameProfile;
import io.github.thatsmusic99.hitwtracker.api.IGameProfile;
import org.jetbrains.annotations.NotNull;

public class HGameProfile implements IGameProfile {

    private final @NotNull GameProfile profile;

    public HGameProfile(final @NotNull GameProfile profile) {
        this.profile = profile;
    }

    @Override
    public @NotNull String getName() {
        return this.profile.getName();
    }

    public @NotNull GameProfile getProfile() {
        return this.profile;
    }
}
