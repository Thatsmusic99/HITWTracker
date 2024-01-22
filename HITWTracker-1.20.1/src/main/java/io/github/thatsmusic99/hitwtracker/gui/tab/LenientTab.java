package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.gui.games.HITWStatScreen;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface LenientTab extends Tab {

    void load(@NotNull HITWStatScreen screen);

    void unload(@NotNull HITWStatScreen screen);
}
