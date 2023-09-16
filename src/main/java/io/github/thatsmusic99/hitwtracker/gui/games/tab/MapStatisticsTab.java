package io.github.thatsmusic99.hitwtracker.gui.games.tab;

import io.github.thatsmusic99.hitwtracker.gui.tab.EntryListTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class MapStatisticsTab extends EntryListTab<MapStatisticsTab.Entry> {
    public MapStatisticsTab(final @NotNull MinecraftClient client,
                            final @NotNull Screen parent,
                            final int top,
                            final int bottom) {
        super(client, parent, top, bottom, 10);
    }

    @Override
    public Text getTitle() {
        return Text.of("Maps");
    }

    public class Entry extends EntryListTab<Entry>.Entry {

        @Override
        public Text getNarration() {
            return null;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

        }
    }
}
