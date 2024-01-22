package io.github.thatsmusic99.hitwtracker.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

public interface IEntryListTab<E, C extends IColumn<E>> {

    default IColumn<E> createColumn(String name, Function<IEntry<E>, String> value, Comparator<IEntry<E>> comparator) {
        return createColumn(name, stat -> false, null, value, comparator);
    }

    IColumn<E> createColumn(final @NotNull String name,
                                final Predicate<IEntry<E>> highlighted,
                                final @Nullable Function<IEntry<E>, String> onHover,
                                final @NotNull Function<IEntry<E>, String> value,
                                final @NotNull Comparator<IEntry<E>> comparator);
    void addColumn(final @NotNull C column);

    void addEntryAtEnd(final @NotNull E entry);

    void addEntryAtTop(final @NotNull E entry);

    void setScrollAmount(int scroll);

    void setupGrid();

    void sortEntries(final @NotNull Comparator<IEntry<E>> comparator);
}
