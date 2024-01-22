package io.github.thatsmusic99.hitwtracker.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Function;

public interface GuiManager {


    default <E> IEntryListTab<E, IColumn<E>> createTab(final @NotNull String name, int columnSpacing) {
        return createTab(name, columnSpacing, null);
    }

    <E> IEntryListTab<E, IColumn<E>> createTab(String name, int columnSpacing, final @Nullable Function<E, String> entryToName);

    int getWidth();

    void renderPlayerFace(final @NotNull String name, int x, int y);
}
