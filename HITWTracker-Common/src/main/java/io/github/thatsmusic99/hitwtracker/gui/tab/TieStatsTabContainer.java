package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.CoreContainer;
import io.github.thatsmusic99.hitwtracker.gui.IColumn;
import io.github.thatsmusic99.hitwtracker.gui.IEntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TieStatsTabContainer {

    private final IEntryListTab<StatisticManager.TieStatistic, IColumn<StatisticManager.TieStatistic>> tab;
    private final IColumn<StatisticManager.TieStatistic> TIES_COLUMN;

    public TieStatsTabContainer() {

        final var guiManager = CoreContainer.get().getGuiManager();

        this.tab = guiManager.createTab("Ties", 10, StatisticManager.TieStatistic::getPlayer);

        this.tab.addColumn(this.tab.createColumn("Player",
                stat -> stat.getItem().getPlayer(),
                Comparator.comparing(stat -> stat.getItem().getPlayer())));

        this.tab.addColumn(TIES_COLUMN = this.tab.createColumn("Ties",
                stat -> String.valueOf(stat.getItem().getCount()),
                Comparator.comparing(stat -> stat.getItem().getCount())));

        StatisticManager.get().getTieStats().whenComplete((stats, err2) -> {

            List<String> maps = new ArrayList<>();

            for (StatisticManager.TieStatistic stat : stats) {
                for (String map : stat.getTiedMaps()) {
                    if (maps.contains(map)) continue;
                    maps.add(map);

                    final IColumn<StatisticManager.TieStatistic> column = this.tab.createColumn(MiscUtils.capitalise(map),
                            stat2 -> String.valueOf(stat2.getItem().getMapCount(map)),
                            Comparator.comparing(g -> g.getItem().getMapCount(map)));

                    this.tab.addColumn(column);
                }

                this.tab.addEntryAtEnd(stat);
            }

            this.tab.setupGrid();

            TIES_COLUMN.setDirection(0);
            this.tab.setScrollAmount(0);
        });
    }

    public IEntryListTab<StatisticManager.TieStatistic, IColumn<StatisticManager.TieStatistic>> getTab() {
        return tab;
    }
}
