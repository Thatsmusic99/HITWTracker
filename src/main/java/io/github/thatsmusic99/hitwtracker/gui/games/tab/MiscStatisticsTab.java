package io.github.thatsmusic99.hitwtracker.gui.games.tab;

import io.github.thatsmusic99.hitwtracker.gui.tab.EntryListTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class MiscStatisticsTab extends EntryListTab<MiscStatisticsTab.Entry> {

    private final @NotNull Column STATISTIC_COLUMN;
    private final @NotNull Column VALUE_COLUMN;

    public MiscStatisticsTab(final @NotNull MinecraftClient client,
                             final @NotNull Screen parent,
                             final int top,
                             final int bottom) {
        super(client, parent, top, bottom, 10);

        GridWidget.Adder adder = this.grid.setColumnSpacing(100).createAdder(2);
        adder.add(STATISTIC_COLUMN = of("", (g1, g2) -> 0));
        adder.add(VALUE_COLUMN = of("", (g1, g2) -> 0));
    }

    @Override
    public Text getTitle() {
        return Text.of("Misc.");
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
