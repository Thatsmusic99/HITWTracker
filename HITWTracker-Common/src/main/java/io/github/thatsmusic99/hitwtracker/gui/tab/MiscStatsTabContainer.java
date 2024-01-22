package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.CoreContainer;
import io.github.thatsmusic99.hitwtracker.gui.IColumn;
import io.github.thatsmusic99.hitwtracker.gui.IEntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;

public class MiscStatsTabContainer {

    private final IEntryListTab<StatisticManager.MiscStatistic, IColumn<StatisticManager.MiscStatistic>> tab;

    public MiscStatsTabContainer() {

        final var guiManager = CoreContainer.get().getGuiManager();

        this.tab = guiManager.createTab("Misc.", 20);

        this.tab.addColumn(this.tab.createColumn("Statistic",
                stat -> stat.getItem().descriptor(),
                (s1, s2) -> 0));

        this.tab.addColumn(this.tab.createColumn("Value",
                stat -> stat.getItem().value(),
                (s1, s2) -> 0));

        this.tab.setupGrid();

        StatisticManager.get().getMiscStats().whenComplete((results, err) -> {

            if (err != null) {
                err.printStackTrace();
                return;
            }

            results.forEach(this.tab::addEntryAtEnd);
        });
    }

    public IEntryListTab<StatisticManager.MiscStatistic, IColumn<StatisticManager.MiscStatistic>> getTab() {
        return tab;
    }
}
