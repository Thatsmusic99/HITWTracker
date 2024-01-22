package io.github.thatsmusic99.hitwtracker.gui;

import io.github.thatsmusic99.hitwtracker.gui.games.HITWStatScreen;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class GuiManagerImpl implements GuiManager {

    @Override
    public <E> IEntryListTab<E, IColumn<E>> createTab(String name, int columnSpacing, @Nullable Function<E, String> entryToName) {
        return new EntryListTab<>(name, MinecraftClient.getInstance(), HITWStatScreen.get(), columnSpacing, entryToName);
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public void renderPlayerFace(@NotNull String name, int x, int y) {

    }
}
