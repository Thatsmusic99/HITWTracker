package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.CoreContainer;
import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.gui.IColumn;
import io.github.thatsmusic99.hitwtracker.gui.IEntry;
import io.github.thatsmusic99.hitwtracker.gui.IEntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AllGamesTabContainer {

    private final IEntryListTab<Statistic, IColumn<Statistic>> tab;
    private final IColumn<Statistic> GAME_COLUMN;

    public AllGamesTabContainer() {

        final var guiManager = CoreContainer.get().getGuiManager();

        this.tab = guiManager.createTab("All", 20);

        this.tab.addColumn(GAME_COLUMN = this.tab.createColumn("Game",
                stat -> stat.getIndex() + (stat.getItem().plobby() ? "*" : ""),
                Comparator.comparing(IEntry::getIndex)));

        this.tab.addColumn(this.tab.createColumn("Placement",
                stat -> String.valueOf(stat.getItem().placement()),
                Comparator.comparing(stat -> stat.getItem().placement())));

        this.tab.addColumn(this.tab.createColumn("Ties",
                stat -> stat.getItem().ties().length > 0,
                stat -> {
                    List<String> names = new ArrayList<>();
                    for (String str : stat.getItem().ties()) {
                        names.add(MiscUtils.getUsername(str));
                    }
                    return String.join(", ", names);
                },
                stat -> String.valueOf(stat.getItem().ties().length),
                Comparator.comparing(stat -> stat.getItem().ties().length)));

        this.tab.addColumn(this.tab.createColumn("Death Cause",
                stat -> stat.getItem().deathCause(),
                Comparator.comparing(stat -> stat.getItem().deathCause())));

        this.tab.addColumn(this.tab.createColumn("Time",
                stat -> MiscUtils.toTime(stat.getItem().seconds()),
                Comparator.comparing(stat -> stat.getItem().seconds())));

        this.tab.addColumn(this.tab.createColumn("Walls",
                stat -> String.valueOf(stat.getItem().walls()),
                Comparator.comparing(stat -> stat.getItem().walls())));

        this.tab.addColumn(this.tab.createColumn("Map",
                stat -> MiscUtils.capitalise(stat.getItem().map()),
                Comparator.comparing(stat -> stat.getItem().map())));

        this.tab.addColumn(this.tab.createColumn("Date",
                stat -> new SimpleDateFormat("dd/MM/yyyy").format(stat.getItem().date()),
                Comparator.comparing(stat -> stat.getItem().date())));

        this.tab.setupGrid();

        StatisticManager.get().getAllStats().whenComplete((stats, err) -> {

            for (Statistic stat : stats) {
                this.tab.addEntryAtTop(stat);
            }

            this.GAME_COLUMN.setDirection(0);
            this.tab.setScrollAmount(0);
        });
    }

    public IEntryListTab<Statistic, IColumn<Statistic>> getTab() {
        return tab;
    }
}
