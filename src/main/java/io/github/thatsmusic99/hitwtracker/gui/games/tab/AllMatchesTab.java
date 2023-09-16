package io.github.thatsmusic99.hitwtracker.gui.games.tab;

import io.github.thatsmusic99.hitwtracker.HITWTracker;
import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.gui.tab.EntryListTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.*;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Comparator;

public class AllMatchesTab extends EntryListTab<AllMatchesTab.Entry> {

    private final @NotNull Column GAME_COLUMN;
    private final @NotNull TextWidget PLACEMENT_COLUMN;
    private final @NotNull TextWidget TIES_COLUMN;
    private final @NotNull TextWidget DEATH_COLUMN;
    private final @NotNull TextWidget TIME_COLUMN;
    private final @NotNull TextWidget WALLS_COLUMN;
    private final @NotNull TextWidget MAP_COLUMN;
    private final @NotNull TextWidget DATE_COLUMN;

    public AllMatchesTab(MinecraftClient minecraftClient, @NotNull Screen parent, int top, int bottom, int m) {
        super(minecraftClient, parent, top, bottom, m);

        GridWidget.Adder adder = this.grid.setColumnSpacing(20).createAdder(8);
        adder.add(GAME_COLUMN = of("Game", Comparator.comparing(g -> g.index)));
        adder.add(PLACEMENT_COLUMN = of("Placement", Comparator.comparing(g -> g.statistic.placement())));
        adder.add(TIES_COLUMN = of("Ties", Comparator.comparing(g -> g.statistic.ties().length)));
        adder.add(DEATH_COLUMN = of("Death Cause", Comparator.comparing(g -> g.statistic.deathCause())));
        adder.add(TIME_COLUMN = of("Time", Comparator.comparing(g -> g.statistic.seconds())));
        adder.add(WALLS_COLUMN = of("Walls", Comparator.comparing(g -> g.statistic.walls())));
        adder.add(MAP_COLUMN = of("Map", Comparator.comparing(g -> g.statistic.map())));
        adder.add(DATE_COLUMN = of("Date", Comparator.comparing(g -> g.statistic.date())));

        HITWTracker.get().getStatsManager().getAllStats().whenCompleteAsync((stats, err) -> {
            if (err != null) {
                err.printStackTrace();
                return;
            }

            int game = 1;
            for (Statistic stat : stats) {
                addEntryToTop(new Entry(stat, game++));
            }

            GAME_COLUMN.setDirection(0);
            setScrollAmount(0);
        });

        // addEntry(new Entry(new Statistic(1, new String[]{"Thatsmusic99"}, "CLASSIC", "", 34, 240, 600, new Date())));
    }

    @Override
    public Text getTitle() {
        return Text.of("All");
    }

    private @NotNull Text ties(String[] ties, int colour) {
        if (ties.length == 0) return MutableText.of(new LiteralTextContent("0")).fillStyle(Style.EMPTY.withColor(colour));

        //
        final var textContent = new LiteralTextContent(String.valueOf(ties.length));
        final var mutableText = MutableText.of(textContent);

        var style = mutableText.getStyle();

        style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.of(String.join(", ", ties))));
        style = style.withUnderline(true);
        style = style.withColor(TextColor.fromRgb(0xffff66));

        return mutableText.setStyle(style);
    }

    @Override
    protected void renderDecorations(DrawContext context, int mouseX, int mouseY) {
        super.renderDecorations(context, mouseX, mouseY);

        // Check if the mouse is hovering over an entry
        if (mouseY >= this.top && mouseY < this.bottom) {
            Entry entry = this.getHoveredEntry();
            if (entry == null) return;

            // See if there's a hover
        }
    }

    protected void renderText() {

    }

    public class Entry extends EntryListTab<Entry>.Entry {

        private final @NotNull Statistic statistic;
        private int index;

        public Entry(final @NotNull Statistic statistic, int index) {
            this.statistic = statistic;
            this.index = index;
        }

        @Override
        public Text getNarration() {
            return Text.of("why");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            final int colour = index % 2 == 1 ? 0xffffff : 11184810;

            draw(context, String.valueOf(this.index), GAME_COLUMN, y, colour);
            draw(context, String.valueOf(statistic.placement()), PLACEMENT_COLUMN, y, colour);
            draw(context, ties(statistic.ties(), colour), TIES_COLUMN, y, colour);
            draw(context, statistic.deathCause(), DEATH_COLUMN, y, colour);
            draw(context, toTime(statistic.seconds()), TIME_COLUMN, y, colour);
            draw(context, String.valueOf(statistic.walls()), WALLS_COLUMN, y, colour);
            draw(context, statistic.map(), MAP_COLUMN, y, colour);
            draw(context, new SimpleDateFormat("dd/MM/yyyy").format(statistic.date()), DATE_COLUMN, y, colour);
        }

        public int getIndex() {
            return index;
        }
    }
}
