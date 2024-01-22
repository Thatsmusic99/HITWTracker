package io.github.thatsmusic99.hitwtracker.gui.games.tab;

import io.github.thatsmusic99.hitwtracker.HITWTracker;
import io.github.thatsmusic99.hitwtracker.client.HITWTrackerClient;
import io.github.thatsmusic99.hitwtracker.gui.tab.EntryListTab;
import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.HashMap;

public class TieStatisticsTab extends EntryListTab<TieStatisticsTab.Entry> {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(TieStatisticsTab.class);
    private final @NotNull OrderableColumn PLAYER_COLUMN;
    private final @NotNull OrderableColumn TIES_COLUMN;
    private final @NotNull HashMap<String, OrderableColumn> MAP_COLUMNS;

    public TieStatisticsTab(final @NotNull MinecraftClient client,
                            final @NotNull Screen parent,
                            final int top,
                            final int bottom) {
        super(client, parent, top, bottom, 10);

        MAP_COLUMNS = new HashMap<>();

        PLAYER_COLUMN = of("Player", Comparator.comparing(g -> g.stat.getPlayer()));
        TIES_COLUMN = of("Ties", Comparator.comparing(g -> g.stat.getCount()));

        // Fetch the ties themselves
        HITWTracker.get().getStatsManager().getTieStats().whenComplete((stats, err2) -> {

            for (StatisticManager.TieStatistic stat : stats) {
                for (String map : stat.getTiedMaps()) {
                    if (MAP_COLUMNS.containsKey(map)) continue;

                    final var column = of(MiscUtils.capitalise(map), Comparator.comparing(g -> g.stat.getMapCount(map)));

                    MAP_COLUMNS.put(map, column);
                }

                addEntry(new Entry(stat));
            }

            GridWidget.Adder adder = this.grid.setColumnSpacing(10).createAdder(MAP_COLUMNS.size() + 2);
            adder.add(PLAYER_COLUMN);
            adder.add(TIES_COLUMN);
            MAP_COLUMNS.values().forEach(adder::add);

            TIES_COLUMN.setDirection(0);
            setScrollAmount(0);
        });
    }

    @Override
    public Text getTitle() {
        return Text.of("Ties");
    }

    public class Entry extends EntryListTab<Entry>.Entry {

        private final @NotNull StatisticManager.TieStatistic stat;
        private boolean rendered = false;

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

            draw(context, stat.getPlayer(), PLAYER_COLUMN, y, colour);
            draw(context, String.valueOf(stat.getCount()), TIES_COLUMN, y, colour);

            // Add ties
            for (final String map : MAP_COLUMNS.keySet()) {
                draw(context, String.valueOf(stat.getMapCount(map)), MAP_COLUMNS.get(map), y, colour);
            }

            context.drawTexture(HITWTrackerClient.get().getProfileManager().get(stat.getPlayer()),
                    PLAYER_COLUMN.getX() - 16, y, 0, 0, 8, 8, 8, 8);

            if (!rendered) {
                grid.refreshPositions();
                rendered = true;
            }
        }
    }
}
