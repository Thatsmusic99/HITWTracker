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

public class MapStatisticsTab extends EntryListTab<MapStatisticsTab.Entry> {

    private final @NotNull OrderableColumn MAP_COLUMN;
    private final @NotNull OrderableColumn GAMES_COLUMN;
    private final @NotNull OrderableColumn AVERAGE_PLACEMENT_COLUMN;
    private final @NotNull OrderableColumn TOP_DEATH_CAUSE_COLUMN;
    private final @NotNull OrderableColumn TIES_COLUMN;
    private final @NotNull OrderableColumn LARGEST_TIE_COLUMN;
    private final @NotNull OrderableColumn MOST_TIED_WITH_COLUMN;
    private final @NotNull OrderableColumn WINS_COLUMN;
    private final @NotNull OrderableColumn TOP_THREES_COLUMN;
    private final @NotNull OrderableColumn WALLS_COLUMN;
    private final @NotNull OrderableColumn AVERAGE_TIME_COLUMN;
    private final @NotNull OrderableColumn FASTEST_TIME_COLUMN;

    public MapStatisticsTab(final @NotNull MinecraftClient client,
                            final @NotNull Screen parent,
                            final int top,
                            final int bottom) {
        super(client, parent, top, bottom, 10);

        GridWidget.Adder adder = this.grid.setColumnSpacing(20).createAdder(13);
        adder.add(MAP_COLUMN = of("Map",
                (e1, e2) -> e1.stat.getMap().equals("Overall") ? -1
                        : e2.stat.getMap().equals("Overall") ? 1 : e1.stat.getMap().compareTo(e2.stat.getMap())));
        adder.add(GAMES_COLUMN = of("Games", Comparator.comparing(g -> g.stat.getGames())));
        adder.add(AVERAGE_PLACEMENT_COLUMN = of("Avg. Placement", Comparator.comparing(g -> g.stat.getAvgPlacement())));
        adder.add(TOP_DEATH_CAUSE_COLUMN = of("Top Death Cause", Comparator.comparing(g -> g.stat.getTopDeathCause())));
        adder.add(TIES_COLUMN = of("Ties", Comparator.comparing(g -> g.stat.getTies())));
        adder.add(LARGEST_TIE_COLUMN = of("Largest Tie", Comparator.comparing(g -> g.stat.getLargestTie())));
        adder.add(MOST_TIED_WITH_COLUMN = of("Most Tied With", Comparator.comparing(g -> g.stat.getMostTiedWith())));
        adder.add(WINS_COLUMN = of("Wins", Comparator.comparing(g -> g.stat.getWins())));
        adder.add(TOP_THREES_COLUMN = of("Top Threes", Comparator.comparing(g -> g.stat.getTopThrees())));
        adder.add(WALLS_COLUMN = of("Walls", Comparator.comparing(g -> g.stat.getWalls())));
        adder.add(AVERAGE_TIME_COLUMN = of("Avg. Time", Comparator.comparing(g -> g.stat.getAverageTime())));
        adder.add(FASTEST_TIME_COLUMN = of("Fastest Win", Comparator.comparing(g -> g.stat.getFastestTime())));

        HITWTracker.get().getStatsManager().getMapStats().whenComplete((results, err) -> {

            if (err != null) {
                err.printStackTrace();
                return;
            }

            results.values().forEach(stat -> addEntry(new Entry(stat)));

            MAP_COLUMN.setDirection(1);
            setScrollAmount(0);
        });
    }

    @Override
    public Text getTitle() {
        return Text.of("Maps");
    }

    public class Entry extends EntryListTab<Entry>.Entry {

        private final @NotNull StatisticManager.MapStatistic stat;

        public Entry(final @NotNull StatisticManager.MapStatistic stat) {
            this.stat = stat;
        }

        @Override
        public Text getNarration() {
            return Text.of("");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            final int colour = index % 2 == 1 ? 0xffffff : 11184810;

            draw(context, stat.getMap(), MAP_COLUMN, y, colour);
            draw(context, String.valueOf(stat.getGames()), GAMES_COLUMN, y, colour);
            draw(context, String.format("%.1f", stat.getAvgPlacement()), AVERAGE_PLACEMENT_COLUMN, y, colour);
            draw(context, stat.getTopDeathCause(), TOP_DEATH_CAUSE_COLUMN, y, colour);
            draw(context, String.valueOf(stat.getTies()), TIES_COLUMN, y, colour);
            draw(context, String.valueOf(stat.getLargestTie()), LARGEST_TIE_COLUMN, y, colour);
            draw(context, stat.getMostTiedWith(), MOST_TIED_WITH_COLUMN, y, colour);
            draw(context, String.valueOf(stat.getWins()),WINS_COLUMN, y, colour);
            draw(context, String.valueOf(stat.getTopThrees()), TOP_THREES_COLUMN, y, colour);
            draw(context, String.valueOf(stat.getWalls()), WALLS_COLUMN, y, colour);
            draw(context, toTime(stat.getAverageTime()), AVERAGE_TIME_COLUMN, y, colour);
            draw(context, toTime(stat.getFastestTime()), FASTEST_TIME_COLUMN, y, colour);

        }
    }
}
