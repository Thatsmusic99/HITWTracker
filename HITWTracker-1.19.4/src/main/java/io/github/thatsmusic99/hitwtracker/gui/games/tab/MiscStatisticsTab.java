package io.github.thatsmusic99.hitwtracker.gui.games.tab;

import io.github.thatsmusic99.hitwtracker.HITWTracker;
import io.github.thatsmusic99.hitwtracker.gui.tab.EntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.util.math.MatrixStack;
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
        adder.add(STATISTIC_COLUMN = of("Statistic", (g1, g2) -> 0));
        adder.add(VALUE_COLUMN = of("Value", (g1, g2) -> 0));

        HITWTracker.get().getStatsManager().getMiscStats().whenComplete((results, err) -> {

            if (err != null) {
                err.printStackTrace();
                return;
            }

            results.forEach(stat -> addEntry(new Entry(stat)));
        });
    }

    @Override
    public Text getTitle() {
        return Text.of("Misc.");
    }

    public class Entry extends EntryListTab<Entry>.Entry {

        private final @NotNull StatisticManager.MiscStatistic stat;

        public Entry(final @NotNull StatisticManager.MiscStatistic stat) {
            this.stat = stat;
        }

        @Override
        public Text getNarration() {
            return Text.of("");
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            final int colour = index % 2 == 1 ? 0xffffff : 11184810;

            draw(matrices, stat.descriptor(), STATISTIC_COLUMN, y, colour);
            draw(matrices, stat.value(), VALUE_COLUMN, y, colour);
        }
    }
}
