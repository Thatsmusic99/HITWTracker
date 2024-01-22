package io.github.thatsmusic99.hitwtracker.game;

import io.github.thatsmusic99.hitwtracker.manager.StatisticManager;
import io.github.thatsmusic99.hitwtracker.util.GameSaver;
import io.github.thatsmusic99.hitwtracker.util.HITWExecutor;
import io.github.thatsmusic99.hitwtracker.util.interfaces.DamageTracking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class Game {

    private final @NotNull DamageTracking<?> player;
    private final @Nullable String mapName;
    private @Nullable String deathReason;
    private @Nullable String[] ties = null;
    private long startTime = -1;
    private short duration = -1;
    private byte placement = -1;
    private byte walls = -1;
    private int traps = -1;
    private short score = -1;
    private boolean dirtyTime;
    private boolean saved;
    private boolean plobby;

    public Game(@Nullable String mapName, @NotNull DamageTracking<?> player) {
        this.mapName = mapName;
        this.player = player;
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
    }

    public void end() {
        if (this.duration != -1) return;
        this.duration = (short) ((System.currentTimeMillis() - this.startTime) / 1000);
        this.dirtyTime = true;

        this.deathReason = Trap.getTrap(() -> this.player).displayName;
    }

    public void setWalls(byte walls) {
        this.walls = walls;
        if (canSave()) save();
    }

    public void setPlacement(byte placement) {
        this.placement = placement;
        if (placement == 1) setDeathReason("");
        if (placement != 1) setTies(new String[0]);
        if (canSave()) save();
    }

    public void setTraps(int traps) {
        this.traps = traps;
    }

    public void setDuration(short duration) {
        this.duration = duration;
        this.dirtyTime = false;
        if (canSave()) save();
    }

    public void setTies(String[] ties) {
        this.ties = ties;
        if (canSave()) save();
    }

    public boolean isPlobby() {
        return plobby;
    }

    public void setPlobby(boolean plobby) {
        this.plobby = plobby;
    }

    public void setDeathReason(@NotNull String deathReason) {
        this.deathReason = deathReason;
    }

    public void setScore(short score) {
        this.score = score;
        if (canSave()) save();
    }

    public boolean canSave() {
        return (this.duration != -1 && !this.dirtyTime) && this.walls != -1 && this.placement != -1 && this.score != -1 && this.ties != null;
    }

    public CompletableFuture<Void> save() {

        if (saved) return CompletableFuture.completedFuture(null);
        if (walls == -1) walls = GameTracker.SECONDS_TO_WALLS.apply(this.duration);

        final String actualMapName = mapName == null ? "N/A" : mapName;
        final String actualDeathReason = deathReason == null ? "Skill Issue" : deathReason;
        final String[] ties = this.ties == null ? new String[0] : this.ties;
        final Date date = new Date();

        // Create the statistic
        final Statistic stat = new Statistic(placement, ties, actualMapName, actualDeathReason, walls, duration, score, plobby, date);
        StatisticManager.get().addStatToCache(stat);
        saved = true;

        // Save it away from the main thread
        return CompletableFuture.runAsync(() -> {
            try {
                GameSaver.saveGame(stat);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, HITWExecutor.executorService);
    }
}
