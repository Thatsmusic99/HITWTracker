package io.github.thatsmusic99.hitwtracker.gui.games.tab;

import io.github.thatsmusic99.hitwtracker.HITWTracker;
import io.github.thatsmusic99.hitwtracker.gui.tab.EntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class TieStatisticsTab extends EntryListTab<TieStatisticsTab.Entry> {

    private final @NotNull Column PLAYER_COLUMN;
    private final @NotNull Column TIES_COLUMN;

    public TieStatisticsTab(final @NotNull MinecraftClient client,
                            final @NotNull Screen parent,
                            final int top,
                            final int bottom) {
        super(client, parent, top, bottom, 10);

        GridWidget.Adder adder = this.grid.setColumnSpacing(80).createAdder(8);
        adder.add(PLAYER_COLUMN = of("Player", Comparator.comparing(g -> g.stat.player())));
        adder.add(TIES_COLUMN = of("Ties", Comparator.comparing(g -> g.stat.count())));

        HITWTracker.get().getStatsManager().getTieStats().whenComplete((stats, err) ->
                stats.forEach(stat -> addEntry(new Entry(stat))));
    }

    @Override
    public Text getTitle() {
        return Text.of("Ties");
    }

    public class Entry extends EntryListTab<Entry>.Entry {

        private final @NotNull StatisticManager.TieStatistic stat;

        public Entry(@NotNull StatisticManager.TieStatistic stat) {
            this.stat = stat;
        }

        @Override
        public Text getNarration() {
            return Text.of("Yes");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            final int colour = index % 2 == 1 ? 0xffffff : 11184810;

            draw(context, stat.player(), PLAYER_COLUMN, y, colour);
            draw(context, String.valueOf(stat.count()), TIES_COLUMN, y, colour);
        }
    }
}
