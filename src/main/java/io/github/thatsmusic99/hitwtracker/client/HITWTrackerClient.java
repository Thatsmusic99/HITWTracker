package io.github.thatsmusic99.hitwtracker.client;

import io.github.thatsmusic99.hitwtracker.manager.ProfileManager;
import net.fabricmc.api.ClientModInitializer;

public class HITWTrackerClient implements ClientModInitializer {

    private static HITWTrackerClient instance;
    private ProfileManager profileManager;

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {
        instance = this;
        this.profileManager = new ProfileManager();

    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    public static HITWTrackerClient get() {
        return instance;
    }
}
