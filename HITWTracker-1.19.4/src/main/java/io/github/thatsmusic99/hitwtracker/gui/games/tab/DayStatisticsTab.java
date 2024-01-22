package io.github.thatsmusic99.hitwtracker.gui.games.tab;

import io.github.thatsmusic99.hitwtracker.HITWTracker;
import io.github.thatsmusic99.hitwtracker.gui.tab.EntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Comparator;

public class DayStatisticsTab extends EntryListTab<DayStatisticsTab.Entry> {

    private final @NotNull Column DATE_COLUMN;
    private final @NotNull TextWidget GAMES_COLUMN;
    private final @NotNull TextWidget AVG_PLACE_COLUMN;
    private final @NotNull TextWidget TIE_COUNT_COLUMN;
    private final @NotNull TextWidget TIE_RATE_COLUMN;
    private final @NotNull TextWidget WIN_COUNT_COLUMN;
    private final @NotNull TextWidget WIN_RATE_COLUMN;
    private final @NotNull TextWidget TOP_THREE_COUNT_COLUMN;
    private final @NotNull TextWidget TOP_THREE_RATE_COLUMN;
    private final @NotNull TextWidget WALLS_COLUMN;
    private final @NotNull TextWidget WALLS_PER_WIN_COLUMN;
    private final @NotNull TextWidget AVERAGE_TIME_COLUMN;
    private final @NotNull TextWidget SHORTEST_TIME_COLUMN;
    public DayStatisticsTab(MinecraftClient minecraftClient, @NotNull Screen parent, int top, int bottom, int m) {
        super(minecraftClient, parent, top, bottom, m);

        GridWidget.Adder adder = this.grid.setColumnSpacing(20).createAdder(13);
        adder.add(DATE_COLUMN = of("Date", Comparator.comparing(g -> g.stat.date())));
        adder.add(GAMES_COLUMN = of("Games", Comparator.comparing(g -> g.stat.games())));
        adder.add(AVG_PLACE_COLUMN = of("Avg. Place", Comparator.comparing(g -> g.stat.avgPlacement())));
        adder.add(TIE_COUNT_COLUMN = of("Ties", Comparator.comparing(g -> g.stat.tieCount())));
        adder.add(TIE_RATE_COLUMN = of("Tie Rate", Comparator.comparing(g -> g.stat.tieRate())));
        adder.add(WIN_COUNT_COLUMN = of("Wins", Comparator.comparing(g -> g.stat.winCount())));
        adder.add(WIN_RATE_COLUMN = of("Win Rate", Comparator.comparing(g -> g.stat.winRate())));
        adder.add(TOP_THREE_COUNT_COLUMN = of("Top 3's", Comparator.comparing(g -> g.stat.topThreeCount())));
        adder.add(TOP_THREE_RATE_COLUMN = of("Top 3 Rate", Comparator.comparing(g -> g.stat.topThreeRate())));
        adder.add(WALLS_COLUMN = of("Walls", Comparator.comparing(g -> g.stat.walls())));
        adder.add(WALLS_PER_WIN_COLUMN = of("Walls/Win", Comparator.comparing(g -> g.stat.wallsPerWin())));
        adder.add(AVERAGE_TIME_COLUMN = of("Avg. Time", Comparator.comparing(g -> g.stat.averageTime())));
        adder.add(SHORTEST_TIME_COLUMN = of("Shortest Win", Comparator.comparing(g -> g.stat.fastestWin())));

        HITWTracker.get().getStatsManager().getDailyStats().whenCompleteAsync((results, err) -> {
            if (err != null) {
                err.printStackTrace();
                return;
            }

            results.values().forEach(stat -> addEntry(new Entry(stat)));

            DATE_COLUMN.setDirection(0);
            setScrollAmount(0);
        });
    }

    @Override
    public Text getTitle() {
        return Text.of("Daily");
    }

    public class Entry extends EntryListTab<Entry>.Entry {

        private final @NotNull StatisticManager.DayStatistic stat;

        public Entry(@NotNull StatisticManager.DayStatistic stat) {
            this.stat = stat;
        }

        @Override
        public Text getNarration() {
            return Text.of("yes");
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            final int colour = index % 2 == 1 ? 0xffffff : 11184810;

            draw(matrices, new SimpleDateFormat("dd/MM/yyyy").format(stat.date()), DATE_COLUMN, y, colour);
            draw(matrices, String.valueOf(stat.games()), GAMES_COLUMN, y, colour);
            draw(matrices, String.format("%.1f", stat.avgPlacement()), AVG_PLACE_COLUMN, y, colour);
            draw(matrices, String.valueOf(stat.tieCount()), TIE_COUNT_COLUMN, y, colour);
            draw(matrices, String.format("%.2f", stat.tieRate() * 100) + "%", TIE_RATE_COLUMN, y, colour);
            draw(matrices, String.valueOf(stat.winCount()), WIN_COUNT_COLUMN, y, colour);
            draw(matrices, String.format("%.2f", stat.winRate() * 100) + "%", WIN_RATE_COLUMN, y, colour);
            draw(matrices, String.valueOf(stat.topThreeCount()), TOP_THREE_COUNT_COLUMN, y, colour);
            draw(matrices, String.format("%.2f", stat.topThreeRate() * 100) + "%", TOP_THREE_RATE_COLUMN, y, colour);
            draw(matrices, String.valueOf(stat.walls()), WALLS_COLUMN, y, colour);
            draw(matrices, String.format("%.1f", stat.wallsPerWin()), WALLS_PER_WIN_COLUMN, y, colour);
            draw(matrices, toTime(stat.averageTime()), AVERAGE_TIME_COLUMN, y, colour);
            draw(matrices, toTime(stat.fastestWin()), SHORTEST_TIME_COLUMN, y, colour);

        }
    }
}
