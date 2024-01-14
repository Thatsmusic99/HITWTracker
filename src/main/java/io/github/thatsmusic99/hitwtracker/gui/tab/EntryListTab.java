package io.github.thatsmusic99.hitwtracker.gui.tab;

import io.github.thatsmusic99.hitwtracker.gui.games.HITWStatScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.function.Consumer;

public abstract class EntryListTab<E extends AlwaysSelectedEntryListWidget.Entry<E>> extends AlwaysSelectedEntryListWidget<E> implements Tab, LenientTab {

    protected final @NotNull GridWidget grid;
    protected final @NotNull Screen parent;
    protected double horizontalScrollAmount;
    protected boolean scrollingHorizontal;
    protected int startingSide;

    public EntryListTab(MinecraftClient minecraftClient, @NotNull Screen parent, int k, int l, int m) {
        super(minecraftClient, parent.width, parent.height, k, l, m);
        this.grid = new GridWidget();
        this.parent = parent;

        setRenderHeader(false, 0);
        // setRenderHorizontalShadows(false);
        setRenderBackground(false);
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
    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        super.updateScrollingState(mouseX, mouseY, button);
        this.scrollingHorizontal = button == 0 && mouseY >= this.getHorizontalScrollbarPositionY()
                && mouseY < this.getHorizontalScrollbarPositionY() + 6;
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

    protected void sortEntries(Comparator<E> comparator) {
        this.children().sort(comparator);
    }

    protected @NotNull OrderableColumn of(String text, Comparator<E> comparator) {

        final var textContent = new LiteralTextContent(text);
        var mutableText = MutableText.of(textContent);
        mutableText = mutableText.setStyle(mutableText.getStyle().withBold(true).withColor(0xffffff));

        final var textWidget = new OrderableColumn(mutableText, comparator);

        textWidget.setWidth(Math.max(40, textWidget.getWidth()));

        return textWidget;
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        this.grid.forEachChild(consumer);
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

    protected static String toTime(int seconds) {
        return (seconds / 60) + ":" + (seconds % 60 < 10 ? "0" + (seconds % 60) : (seconds % 60));
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

    public abstract class Entry extends AlwaysSelectedEntryListWidget.Entry<E> {

        protected void draw(DrawContext context, String text, TextWidget column, int y, int colour) {
            this.draw(context, Text.of(text), column, y, colour);
        }

        protected void draw(DrawContext context, Text text, TextWidget column, int y, int colour) {
            context.drawTextWithShadow(client.textRenderer, text, centre(text, column), y, colour);
        }

        private int centre(Text text, TextWidget column) {

            TextWidget widget = new TextWidget(text, client.textRenderer);

            // Get the difference in width
            int deltaWidth = column.getWidth() - widget.getWidth();
            if (deltaWidth < 0) return column.getX();

            // Half and set position
            return deltaWidth / 2 + column.getX();
        }
    }

    public class OrderableColumn extends TextWidget {

        private static final int DIRECTION_DOWN = 0;
        private static final int DIRECTION_UP = 1;
        private static final int DIRECTION_NONE = 2;
        private final @NotNull Comparator<E> comparator;
        private final @NotNull Comparator<E> reverseComparator;
        private final @NotNull Text defaultMessage;
        private final @NotNull Text messageUp;
        private final @NotNull Text messageDown;
        private int direction = 2;

        public OrderableColumn(@NotNull Text message, Comparator<E> comparator) {
            super(message, client.textRenderer);
            this.comparator = comparator;
            this.reverseComparator = comparator.reversed();
            this.defaultMessage = message;
            this.messageUp = Text.literal(message.getString() + " \uD83D\uDD3C").setStyle(message.getStyle());
            this.messageDown = Text.literal(message.getString() + " \uD83D\uDD3D").setStyle(message.getStyle());
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            setDirection((this.direction + 1) % 2);
        }

        public void setDirection(int direction) {
            this.direction = direction;
            switch (direction) {
                case DIRECTION_NONE -> setMessage(defaultMessage);
                case DIRECTION_DOWN -> setMessage(messageDown);
                case DIRECTION_UP -> setMessage(messageUp);
            }

            if (this.direction != DIRECTION_NONE) {
                if (this.direction == DIRECTION_DOWN) {
                    sortEntries(this.reverseComparator);
                } else {
                    sortEntries(this.comparator);
                }

                grid.forEachChild(widget -> {

                    if (widget instanceof EntryListTab<?>.OrderableColumn column && widget != this) {
                        column.setDirection(DIRECTION_NONE);
                    }
                });
            }
        }
    }
}
