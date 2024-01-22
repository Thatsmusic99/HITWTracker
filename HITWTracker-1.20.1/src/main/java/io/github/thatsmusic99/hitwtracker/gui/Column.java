package io.github.thatsmusic99.hitwtracker.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

public class Column<E, C extends IColumn<E>> extends TextWidget implements IColumn<E> {

    private static final int DIRECTION_DOWN = 0;
    private static final int DIRECTION_UP = 1;
    private static final int DIRECTION_NONE = 2;
    private final EntryListTab<E, C> tab;
    private final @NotNull Function<IEntry<E>, String> toString;
    private final @Nullable Function<IEntry<E>, String> onHover;
    private final @NotNull Comparator<IEntry<E>> comparator;
    private final @NotNull Comparator<IEntry<E>> reverseComparator;
    private final @NotNull Text defaultMessage;
    private final @NotNull Text messageUp;
    private final @NotNull Text messageDown;
    private final @NotNull Predicate<IEntry<E>> highlighted;
    private int direction = 2;

    public Column(final EntryListTab<E, C> tab,
                  final @NotNull String name,
                  final @NotNull Predicate<IEntry<E>> highlighted,
                  final @Nullable Function<IEntry<E>, String> onHover,
                  final @NotNull Function<IEntry<E>, String> toString,
                  final @NotNull Comparator<IEntry<E>> comparator) {
        super(Text.of(name), MinecraftClient.getInstance().textRenderer);
        this.defaultMessage = Text.of(name);
        this.messageUp = Text.literal(name + " \uD83D\uDD3C");
        this.messageDown = Text.literal(name + " \uD83D\uDD3D");

        this.comparator = comparator;
        this.reverseComparator = comparator.reversed();

        this.toString = toString;
        this.onHover = onHover;

        this.highlighted = highlighted;

        this.tab = tab;

        setWidth(Math.max(40, getWidth() + 20));
    }

    @Override
    public void render(final @NotNull IEntry<E> entry, boolean bold) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setDirection((direction + 1) % 2);
    }

    @Override
    public void setDirection(int direction) {

        this.direction = direction;
        switch (direction) {
            case DIRECTION_NONE -> setMessage(defaultMessage);
            case DIRECTION_DOWN -> setMessage(messageDown);
            case DIRECTION_UP -> setMessage(messageUp);
        }

        if (this.direction != DIRECTION_NONE) {
            if (this.direction == DIRECTION_DOWN) {
                this.tab.sortEntries(this.reverseComparator);
            } else {
                this.tab.sortEntries(this.comparator);
            }

            this.tab.forEachChild(widget -> {

                if (widget instanceof Column<?, ?> column && widget != this) {
                    column.setDirection(DIRECTION_NONE);
                }
            });
        }
    }

    public @NotNull Text of(final @NotNull IEntry<E> entry) {

        if (this.highlighted.test(entry)) {

            final var textContent = new LiteralTextContent(this.toString.apply(entry));
            final var mutableText = MutableText.of(textContent);

            var style = mutableText.getStyle();

            style = style.withUnderline(true);
            style = style.withColor(TextColor.fromRgb(0xffff66));

            return mutableText.setStyle(style);
        }

        return Text.literal(toString.apply(entry));
    }

    public void onHover(final @NotNull IEntry<E> entry, final @NotNull DrawContext context, final int x, final int y) {
        if (this.onHover == null) return;
        final String result = this.onHover.apply(entry);
        if (result == null || result.isEmpty()) return;
        context.drawTooltip(MinecraftClient.getInstance().textRenderer, Text.of(result), x, y);
    }

    @Override
    public int getXCoord() {
        return this.getX();
    }

    @Override
    public int getWidgetWidth() {
        return this.getWidth();
    }

    @Override
    public boolean hasHover() {
        return this.onHover != null;
    }
}
