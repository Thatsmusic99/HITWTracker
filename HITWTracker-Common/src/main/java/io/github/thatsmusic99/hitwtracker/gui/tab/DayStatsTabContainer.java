package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.CoreContainer;
import io.github.thatsmusic99.hitwtracker.gui.IColumn;
import io.github.thatsmusic99.hitwtracker.gui.IEntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;

import java.text.SimpleDateFormat;
import java.util.Comparator;

public class DayStatsTabContainer {

    private final IEntryListTab<StatisticManager.DayStatistic, IColumn<StatisticManager.DayStatistic>> tab;
    private final IColumn<StatisticManager.DayStatistic> DATE_COLUMN;

    public DayStatsTabContainer() {

        final var guiManager = CoreContainer.get().getGuiManager();

        this.tab = guiManager.createTab("Daily", 20);

        this.tab.addColumn(DATE_COLUMN = this.tab.createColumn("Date",
                stat -> new SimpleDateFormat("dd/MM/yyyy").format(stat.getItem().date()),
                Comparator.comparing(stat -> stat.getItem().date())));

        this.tab.addColumn(this.tab.createColumn("Games",
                stat -> String.valueOf(stat.getItem().games()),
                Comparator.comparing(stat -> stat.getItem().games())));

        this.tab.addColumn(this.tab.createColumn("Avg. Place",
                stat -> String.format("%.1f", stat.getItem().avgPlacement()),
                Comparator.comparing(stat -> stat.getItem().avgPlacement())));

        this.tab.addColumn(this.tab.createColumn("Ties",
                stat -> String.format("%d (%.2f%%)", stat.getItem().tieCount(), stat.getItem().tieRate()),
                Comparator.comparing(stat -> stat.getItem().tieCount())));

        this.tab.addColumn(this.tab.createColumn("Wins",
                stat -> String.format("%d (%.2f%%)", stat.getItem().winCount(), stat.getItem().winRate()),
                Comparator.comparing(stat -> stat.getItem().winCount())));

        this.tab.addColumn(this.tab.createColumn("Top 3's",
                stat -> String.format("%d (%.2f%%)", stat.getItem().topThreeCount(), stat.getItem().topThreeRate()),
                Comparator.comparing(stat -> stat.getItem().topThreeCount())));

        this.tab.addColumn(this.tab.createColumn("Walls",
                stat -> String.valueOf(stat.getItem().walls()),
                Comparator.comparing(stat -> stat.getItem().walls())));

        this.tab.addColumn(this.tab.createColumn("Walls/Win",
                stat -> String.format("%.2f", stat.getItem().wallsPerWin()),
                Comparator.comparing(stat -> stat.getItem().wallsPerWin())));

        this.tab.addColumn(this.tab.createColumn("Avg. Time",
                stat -> MiscUtils.toTime(stat.getItem().averageTime()),
                Comparator.comparing(stat -> stat.getItem().averageTime())));

        this.tab.addColumn(this.tab.createColumn("Shortest Win",
                stat -> String.valueOf(stat.getItem().fastestWin()),
                Comparator.comparing(stat -> stat.getItem().fastestWin())));

        this.tab.setupGrid();

        StatisticManager.get().getDailyStats().whenCompleteAsync((results, err) -> {
            if (err != null) {
                err.printStackTrace();
                return;
            }

            results.values().forEach(this.tab::addEntryAtTop);

            DATE_COLUMN.setDirection(0);
            this.tab.setScrollAmount(0);
        });
    }

    public IEntryListTab<StatisticManager.DayStatistic, IColumn<StatisticManager.DayStatistic>> getTab() {
        return tab;
    }
}
