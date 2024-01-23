package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.CoreContainer;
import io.github.thatsmusic99.hitwtracker.gui.IColumn;
import io.github.thatsmusic99.hitwtracker.gui.IEntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;

import java.util.Comparator;
import java.util.Objects;

public class MapStatsTabContainer {

    private final IEntryListTab<StatisticManager.MapStatistic, IColumn<StatisticManager.MapStatistic>> tab;
    private final IColumn<StatisticManager.MapStatistic> MAP_COLUMN;

    public MapStatsTabContainer() {

        final var guiManager = CoreContainer.get().getGuiManager();

        this.tab = guiManager.createTab("Maps", 20);

        this.tab.addColumn(MAP_COLUMN = this.tab.createColumn("Map",
                stat -> stat.getItem().getMap(),
                (e1, e2) -> e1.getItem().getMap().equals("Overall") ? -1
                        : e2.getItem().getMap().equals("Overall") ? 1 : e1.getItem().getMap().compareTo(e2.getItem().getMap())));

        this.tab.addColumn(this.tab.createColumn("Games",
                stat -> String.valueOf(stat.getItem().getGames()),
                Comparator.comparing(stat -> stat.getItem().getGames())));

        this.tab.addColumn(this.tab.createColumn("Avg. Placement",
                stat -> String.format("%.1f", stat.getItem().getAvgPlacement()),
                Comparator.comparing(stat -> stat.getItem().getAvgPlacement())));

        this.tab.addColumn(this.tab.createColumn("Top Death Cause",
                stat -> Objects.requireNonNullElse(stat.getItem().getTopDeathCause(), ""),
                Comparator.comparing(stat -> stat.getItem().getTopDeathCause())));

        this.tab.addColumn(this.tab.createColumn("Ties",
                stat -> String.valueOf(stat.getItem().getTies()),
                Comparator.comparing(stat -> stat.getItem().getTies())));

        this.tab.addColumn(this.tab.createColumn("Largest Tie",
                stat -> String.valueOf(stat.getItem().getLargestTie()),
                Comparator.comparing(stat -> stat.getItem().getLargestTie())));

        this.tab.addColumn(this.tab.createColumn("Most Tied With",
                stat -> Objects.requireNonNullElse(stat.getItem().getMostTiedWith(), ""),
                Comparator.comparing(stat -> stat.getItem().getMostTiedWith())));

        this.tab.addColumn(this.tab.createColumn("Wins",
                stat -> String.valueOf(stat.getItem().getWins()),
                Comparator.comparing(stat -> stat.getItem().getWins())));

        this.tab.addColumn(this.tab.createColumn("Top Threes",
                stat -> String.valueOf(stat.getItem().getTopThrees()),
                Comparator.comparing(stat -> stat.getItem().getTopThrees())));

        this.tab.addColumn(this.tab.createColumn("Walls",
                stat -> String.valueOf(stat.getItem().getWalls()),
                Comparator.comparing(stat -> stat.getItem().getWalls())));

        this.tab.addColumn(this.tab.createColumn("Avg. Time",
                stat -> MiscUtils.toTime(stat.getItem().getAverageTime()),
                Comparator.comparing(stat -> stat.getItem().getAverageTime())));

        this.tab.addColumn(this.tab.createColumn("Fastest Win",
                stat -> MiscUtils.toTime(stat.getItem().getFastestTime()),
                Comparator.comparing(stat -> stat.getItem().getFastestTime())));

        this.tab.setupGrid();

        StatisticManager.get().getMapStats().whenComplete((results, err) -> {

            if (err != null) {
                err.printStackTrace();
                return;
            }

            results.values().forEach(this.tab::addEntryAtEnd);

            MAP_COLUMN.setDirection(1);
            this.tab.setScrollAmount(0);
        });
    }

    public IEntryListTab<StatisticManager.MapStatistic, IColumn<StatisticManager.MapStatistic>> getTab() {
        return tab;
    }
}
