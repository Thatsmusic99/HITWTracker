package io.github.thatsmusic99.hitwtracker.gui;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public interface IColumn<E> {

    void render(final @NotNull IEntry<E> entry, boolean bold);

    void setDirection(int direction);

    int getXCoord();

    int getWidgetWidth();

    boolean hasHover();
}
