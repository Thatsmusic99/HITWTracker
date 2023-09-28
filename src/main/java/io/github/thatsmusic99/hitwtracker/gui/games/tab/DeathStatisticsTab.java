package io.github.thatsmusic99.hitwtracker.gui.games.tab;

import io.github.thatsmusic99.hitwtracker.HITWTracker;
import io.github.thatsmusic99.hitwtracker.gui.tab.EntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class DeathStatisticsTab extends EntryListTab<DeathStatisticsTab.Entry> {

    private final @NotNull Column DEATH_CAUSE_COLUMN;
    private final @NotNull Column COUNT_COLUMN;
    private final @NotNull Column RATE_COLUMN;

    public DeathStatisticsTab(final @NotNull MinecraftClient client,
                              final @NotNull Screen parent,
                              final int top,
                              final int bottom) {
        super(client, parent, top, bottom, 10);

        GridWidget.Adder adder = this.grid.setColumnSpacing(40).createAdder(8);
        adder.add(DEATH_CAUSE_COLUMN = of("Death", Comparator.comparing(g -> g.stat.getReason())));
        adder.add(COUNT_COLUMN = of("Count", Comparator.comparing(g -> g.stat.getCount())));
        adder.add(RATE_COLUMN = of("Rate", Comparator.comparing(g -> g.stat.getCount() / (float) g.stat.getGames())));

        HITWTracker.get().getStatsManager().getDeathStats().whenComplete((stats, err) -> {
            stats.forEach(stat -> addEntry(new Entry(stat)));
            RATE_COLUMN.setDirection(0);
            setScrollAmount(0);
        });
    }

    @Override
    public Text getTitle() {
        return Text.of("Deaths");
    }

    public class Entry extends EntryListTab<Entry>.Entry {

        private final @NotNull StatisticManager.DeathStatistic stat;

        public Entry(@NotNull StatisticManager.DeathStatistic stat) {
            this.stat = stat;
        }

        @Override
        public Text getNarration() {
            return Text.of("Yes");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            final int colour = index % 2 == 1 ? 0xffffff : 11184810;

            draw(context, stat.getReason(), DEATH_CAUSE_COLUMN, y, colour);
            draw(context, String.valueOf(stat.getCount()), COUNT_COLUMN, y, colour);
            draw(context, String.format("%.2f", stat.getCount() / (float) stat.getGames() * 100) + "%", RATE_COLUMN, y, colour);
        }
    }
}
