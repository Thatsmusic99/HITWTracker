package io.github.thatsmusic99.hitwtracker.game;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

public record Statistic(
        byte placement,
        @NotNull String[] ties,
        @NotNull String map,
        @NotNull String deathCause,
        byte walls,
        short seconds,
        short score,
        @NotNull Date date
) {
}
