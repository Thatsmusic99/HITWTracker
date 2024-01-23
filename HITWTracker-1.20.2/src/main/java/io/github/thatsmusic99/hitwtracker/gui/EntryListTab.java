package io.github.thatsmusic99.hitwtracker.gui;

import io.github.thatsmusic99.hitwtracker.client.HITWTrackerClient;
import io.github.thatsmusic99.hitwtracker.gui.games.HITWStatScreen;
import io.github.thatsmusic99.hitwtracker.gui.tab.LenientTab;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class EntryListTab<T, C extends IColumn<T>> extends AbstractEntryListTab<EntryListTab<T, C>.Entry> implements Tab, LenientTab, IEntryListTab<T, C> {

    protected final List<C> columns;
    protected final @NotNull GridWidget grid;
    protected final @NotNull Screen parent;
    protected final @Nullable Function<T, String> useFaces;
    private final int columnSpacing;
    protected double horizontalScrollAmount;
    protected boolean scrollingHorizontal;
    protected int startingSide;
    protected String name;

    public EntryListTab(final @NotNull String name,
                        MinecraftClient minecraftClient,
                        @NotNull Screen parent,
                        int columnSpacing,
                        final @Nullable Function<T, String> faceNames) {
        super(minecraftClient, parent.width, parent.height, 66, parent.height - 36, 10);

        this.columns = new ArrayList<>();
        this.grid = new GridWidget();
        this.parent = parent;
        this.useFaces = faceNames;
        this.columnSpacing = columnSpacing;
        this.name = name;

        setRenderHeader(false, 0);
        setRenderBackground(false);
    }

    @Override
    public IColumn<T> createColumn(
            final @NotNull String name,
            final @NotNull Predicate<IEntry<T>> highlighted,
            final @Nullable Function<IEntry<T>, String> onHover,
            final @NotNull Function<IEntry<T>, String> value,
            final @NotNull Comparator<IEntry<T>> comparator) {
        return new Column<>(this, name, highlighted, onHover, value, comparator);
    }

    @Override
    public void addColumn(@NotNull C column) {
        this.columns.add(column);
    }

    @Override
    public void addEntryAtEnd(@NotNull T entry) {
        addEntry(new Entry(entry, getEntryCount()));
    }

    @Override
    public void addEntryAtTop(@NotNull T entry) {
        addEntryToTop(new Entry(entry, getEntryCount()));
    }

    @Override
    public void setScrollAmount(int scroll) {
        this.setScrollAmount((double) scroll);
    }

    @Override
    public void setupGrid() {

        final var adder = this.grid.setColumnSpacing(columnSpacing).createAdder(this.columns.size());
        this.columns.forEach(column -> adder.add((Column<T, C>) column));
    }

    @Override
    public void load(@NotNull HITWStatScreen screen) {
        this.grid.forEachChild(screen::addDrawableChild);
        screen.addSelectableChild(this);
    }

    @Override
    public void unload(@NotNull HITWStatScreen screen) {
        this.grid.forEachChild(screen::remove);
        screen.remove(this);
    }

    @Override
    public Text getTitle() {
        return Text.of(name);
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        this.grid.forEachChild(consumer);
    }

    @Override
    public void sortEntries(@NotNull Comparator<IEntry<T>> comparator) {
        this.children().sort(comparator);
    }

    @Override
    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        super.updateScrollingState(mouseX, mouseY, button);
        this.scrollingHorizontal = button == 0 && mouseY >= this.getHorizontalScrollbarPositionY()
                && mouseY < this.getHorizontalScrollbarPositionY() + 6;
    }

    @Override
    public void refreshGrid(ScreenRect tabArea) {
        this.bottom = this.bottom + (this.parent.height - this.height);
        this.width = parent.width;
        this.height = parent.height;
        this.right = parent.width;
        this.grid.refreshPositions();
        SimplePositioningWidget.setPos(this.grid, tabArea, 0.5F, 0.05f);

        this.grid.setX(Math.max(20, this.grid.getX()));

        this.startingSide = this.grid.getX();
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.width - 6;
    }

    private int getMaxHorizontalScroll() {
        final int gridLength = this.startingSide + this.grid.getWidth() - this.width;
        return Math.max(0, gridLength + 24);
    }

    public void setHorizontalScrollAmount(double amount) {
        this.horizontalScrollAmount = MathHelper.clamp(amount, 0.0, this.getMaxHorizontalScroll());
    }

    private int getHorizontalScrollbarPositionY() {
        return this.bottom - 6;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        boolean originalResult = super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        if (button != 0 || !this.scrollingHorizontal) return originalResult;

        // If they're scrolling off the screen, don't let it go beyond that
        if (mouseX < this.left) {
            setHorizontalScrollAmount(0);
        } else if (mouseX > this.right) {
            setHorizontalScrollAmount(getMaxHorizontalScroll());
        } else {
            final int width = this.right - this.left;
            final int maxScroll = Math.max(1, getMaxHorizontalScroll());
            final int rawScroll = MathHelper.clamp((int)((float)(width * width) / (float)this.getMaxHorizontalScroll() / 3), 32, width - 8);
            final double sensitivity = Math.max(1.0, maxScroll / ((double) width - rawScroll));
            this.setHorizontalScrollAmount(horizontalScrollAmount + deltaX * sensitivity);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button) || this.scrollingHorizontal) return true;

        // Check each grid element, check if they were clicked
        grid.forEachChild(widget -> {
            if (button == 0 && mouseX >= widget.getX() && mouseX < widget.getX() + widget.getWidth()
                    && mouseY >= widget.getY() && mouseY < widget.getY() + widget.getHeight()) {
                widget.onClick(mouseX, mouseY);
            }
        });
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.setShaderColor(0.125F, 0.125F, 0.125F, 1.0F);
        context.drawTexture(Screen.OPTIONS_BACKGROUND_TEXTURE, this.left, this.top, (float)this.right, (float)(this.bottom + (int)this.getScrollAmount()), this.right - this.left, this.bottom - this.top, 32, 32);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        super.render(context, mouseX, mouseY, delta);

        this.grid.setX(this.startingSide - (int) this.horizontalScrollAmount);

        // If we need to render the horizontal scroll bar, do that
        int scrollLength = getMaxHorizontalScroll();
        if (scrollLength > 0) {

            int startY = getHorizontalScrollbarPositionY();
            int endY = getHorizontalScrollbarPositionY() + 6;

            int width = this.right - this.left;
            int barLength = (int) ((width * width) / (float) this.getMaxHorizontalScroll()) / 3;
            barLength = MathHelper.clamp(barLength, 32, width - 8);

            int start = (int) (horizontalScrollAmount * (width - barLength) / scrollLength + this.left);

            context.fill(this.left, startY, this.right, endY, -16777216);
            context.fill(start, startY, start + barLength, endY, -8355712);
            context.fill(start, startY, start + barLength - 1, endY - 1, -4144960);
        }
        // context.fillGradient(RenderLayer.getGuiOverlay(), this.left, this.top, this.right, this.top, -16777216, 0, 0);
    }

    @Override
    protected void renderDecorations(DrawContext context, int mouseX, int mouseY) {
        super.renderDecorations(context, mouseX, mouseY);

        // Check if the mouse is hovering over an entry
        if (mouseY >= this.top && mouseY < this.bottom) {
            Entry entry = this.getHoveredEntry();
            if (entry == null) return;

            // Check each column
            for (C column : this.columns) {
                if (!column.hasHover()) continue;
                if (mouseX < column.getXCoord() || mouseX > column.getXCoord() + column.getWidgetWidth()) continue;

                ((Column<T, C>) column).onHover(entry, context, mouseX, mouseY);
            }
        }
    }

    public class Entry extends AlwaysSelectedEntryListWidget.Entry<Entry> implements IEntry<T> {

        private final @NotNull T item;
        private final int index;
        private boolean rendered = false;

        public Entry(final @NotNull T item, int index) {
            this.item = item;
            this.index = index;
        }

        protected void draw(DrawContext context, final @NotNull String text, TextWidget column, int y, int colour) {
            this.draw(context, Text.of(text), column, y, colour);
        }

        protected void draw(DrawContext context, final @NotNull Text text, TextWidget column, int y, int colour) {
            context.drawTextWithShadow(client.textRenderer, text, centre(text, column), y, colour);
        }

        private int centre(Text text, TextWidget column) {

            TextWidget widget = new TextWidget(text, client.textRenderer);

            // Get the difference in width
            int deltaWidth = column.getWidth() - widget.getWidth();
            if (deltaWidth < 0) {
                column.setWidth(widget.getWidth());
                return column.getX();
            }

            // Half and set position
            return deltaWidth / 2 + column.getX();
        }

        @Override
        public @NotNull T getItem() {
            return item;
        }

        @Override
        public int getIndex() {
            return index;
        }

        @Override
        public Text getNarration() {
            return Text.of("Yes");
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {

            // Determine if we use the face renderer at any point
            if (useFaces != null) {
                if (columns.isEmpty()) return;
                C column = columns.get(0);
                context.drawTexture(HITWTrackerClient.get().getProfileManager().get(useFaces.apply(item)),
                        column.getXCoord() - 16, y, 0, 0, 8, 8, 8, 8);
            }

            final int colour = index % 2 == 1 ? 0xffffff : 11184810;

            for (C columnRaw : columns) {
                Column<T, C> column = (Column<T, C>) columnRaw;
                draw(context, column.of(this), column, y, colour);
            }

            // If it's not a rendered item, then re-adjust the columns of the grid
            if (!rendered) {
                grid.refreshPositions();
                this.rendered = true;
            }
        }
    }
}
