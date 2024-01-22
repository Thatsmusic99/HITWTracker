package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.gui.games.HITWStatScreen;
import net.minecraft.client.gui.tab.Tab;
import org.jetbrains.annotations.NotNull;

public interface LenientTab extends Tab {

    void load(@NotNull HITWStatScreen screen);

    void unload(@NotNull HITWStatScreen screen);
}
