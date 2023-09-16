package io.github.thatsmusic99.hitwtracker.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.thatsmusic99.hitwtracker.HITWTracker;
import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.serializer.StatisticSerializer;
import io.github.thatsmusic99.hitwtracker.util.GameSaver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StatisticManager {

    private final @NotNull HashMap<Long, List<Statistic>> dailyStats;
    private final @NotNull HashMap<Long, DayStatistic> dayStatistics;
    private final @NotNull List<DeathStatistic> deathStatistics;
    private final @NotNull HashMap<String, MapStatistic> mapStatistics;
    private final @NotNull List<TieStatistic> tieStatistics;
    private final @NotNull List<Long> times;
    private transient @Nullable CompletableFuture<Void> loading;
    private boolean loaded;

    public StatisticManager() {
        this.dailyStats = new HashMap<>();
        this.dayStatistics = new HashMap<>();
        this.deathStatistics = new ArrayList<>();
        this.mapStatistics = new HashMap<>();
        this.tieStatistics = new ArrayList<>();
        this.times = new ArrayList<>();
        this.loaded = false;
    }

    private @NotNull CompletableFuture<Void> load() {
        if (loading != null) return loading;

        return loading = CompletableFuture.runAsync(() -> {

            // Go through each file in the games folder
            final File gamesFolder = new File("games");
            if (!gamesFolder.exists()) return;

            // Go through each file
            final File[] files = gamesFolder.listFiles();
            if (files == null) return;
            for (int i = files.length - 1; i >= 0; i--) {

                // Get the file itself
                final File file = files[i];

                // Read the file
                final String content;
                try {
                    content = GameSaver.getFileContents(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

                final Gson gson = new GsonBuilder().registerTypeAdapter(Statistic.class, new StatisticSerializer()).create();
                final Statistic[] stats = gson.fromJson(content, Statistic[].class);

                // Go through each statistic and get its date
                for (Statistic stat : stats) {

                    final Date timedDate = stat.date();
                    final long time = getDayAtMidnight(timedDate);

                    // Get the list of times for that
                    final List<Statistic> existingStats = this.dailyStats.computeIfAbsent(time, x -> {
                        this.times.add(x);
                        return new ArrayList<>();
                    });
                    existingStats.add(stat);
                    this.dailyStats.put(time, existingStats);
                }
            }

            // Reverse times
            Collections.sort(this.times);

            loading = null;
            loaded = true;
        }, HITWTracker.executorService);
    }

    public @NotNull CompletableFuture<@NotNull List<Statistic>> getAllStats() {

        return getStats(null, x -> true, () -> {

            // Get all matches
            List<Statistic> stats = new ArrayList<>();
            for (long time : this.times) {
                stats.addAll(this.dailyStats.get(time));
            }

            return stats;
        });
    }

    public @NotNull CompletableFuture<@NotNull HashMap<Long, DayStatistic>> getDailyStats() {

        return getStats(this.dayStatistics, () -> {
            final HashMap<Long, DayStatistic> stats = new LinkedHashMap<>();

            for (long time : this.times) {
                stats.put(time, formDayStatistics(time));
            }

            return stats;
        });
    }

    public @NotNull CompletableFuture<@NotNull List<DeathStatistic>> getDeathStats() {
        return getStats(this.deathStatistics, this::formDeathStatistics);
    }

    public @NotNull CompletableFuture<@NotNull List<TieStatistic>> getTieStats() {

        return getStats(this.tieStatistics, this::formTieStatistics);
    }

    private <T extends Map<?, ?>> @NotNull CompletableFuture<T> getStats(T check, Supplier<T> supplier) {
        return getStats(check, x -> x.isEmpty(), supplier);
    }

    private <T extends Collection<?>> @NotNull CompletableFuture<T> getStats(T check, Supplier<T> supplier) {
        return getStats(check, x -> x.isEmpty(), supplier);
    }

    private <T> @NotNull CompletableFuture<T> getStats(@Nullable T check, Predicate<T> isEmpty, Supplier<T> supplier) {

        if (!isEmpty.test(check)) {
            return CompletableFuture.completedFuture(check);
        }

        if (!loaded) {
            return load().thenApplyAsync(x -> supplier.get(), HITWTracker.executorService);
        }

        return CompletableFuture.supplyAsync(supplier, HITWTracker.executorService);
    }

    private @Nullable DayStatistic formDayStatistics(long time) {

        final List<Statistic> stats = this.dailyStats.get(time);
        if (stats == null) return null;

        final Date date = new Date(time);
        final int games = stats.size();

        int placements = 0;
        short ties = 0;
        short wins = 0;
        short topThrees = 0;
        int walls = 0;
        int totalTime = 0;
        short shortestTime = 240;

        // Go through each statistic...
        for (Statistic stat : stats) {
            placements += stat.placement();
            if (stat.ties().length > 0) ties++;
            if (stat.placement() == 1) wins++;
            if (stat.placement() <= 3) topThrees++;
            walls += stat.walls();
            totalTime += stat.seconds();
            shortestTime = (short) Math.min(shortestTime, stat.seconds());
        }

        final float avgPlace = placements / (float) games;
        final float tieRate = ties / (float) games;
        final float winRate = wins / (float) games;
        final float topThreeRate = topThrees / (float) games;
        final float wallsPerWin = walls / (float) wins;
        final short avgTime = (short) (totalTime / games);

        return new DayStatistic(date, games, avgPlace, ties, tieRate, wins, winRate, topThrees, topThreeRate, walls, wallsPerWin, avgTime, shortestTime);

    }

    private @NotNull List<DeathStatistic> formDeathStatistics() {

        // Go through each statistic for each day
        HashMap<String, Integer> deaths = new HashMap<>();
        int games = 0;
        for (long time : this.dailyStats.keySet()) {
            for (Statistic stat : this.dailyStats.get(time)) {

                // If the death reason is empty, skip it
                if (stat.deathCause().isEmpty()) continue;
                deaths.put(stat.deathCause(), deaths.getOrDefault(stat.deathCause(), 0) + 1);
                games++;
            }
        }

        List<DeathStatistic> deathStats = new ArrayList<>();
        for (String deathCause : deaths.keySet()) {
            deathStats.add(new DeathStatistic(deathCause, deaths.get(deathCause), games));
        }

        this.deathStatistics.addAll(deathStats);
        return deathStats;
    }

    private @NotNull List<TieStatistic> formTieStatistics() {

        // Go through each statistic for each day
        HashMap<String, Integer> ties = new HashMap<>();
        for (long time : this.dailyStats.keySet()) {
            for (Statistic stat : this.dailyStats.get(time)) {

                // Go through each name
                for (String name : stat.ties()) {
                    ties.put(name, ties.getOrDefault(name, 0) + 1);
                }
            }
        }

        // Go through each name and create tie stat records
        List<TieStatistic> tieStats = new ArrayList<>();
        for (String name : ties.keySet()) {
            tieStats.add(new TieStatistic(name, ties.get(name)));
        }

        this.tieStatistics.addAll(tieStats);

        return tieStats;
    }

    public void addStatToCache(final @NotNull Statistic statistic) {

        // Get the date to save to
        long time = getDayAtMidnight(statistic.date());

        final List<Statistic> existingStats = this.dailyStats.computeIfAbsent(time, x -> {
            this.times.add(x);
            return new ArrayList<>();
        });

        existingStats.add(statistic);
        this.dailyStats.put(time, existingStats);
    }

    private long getDayAtMidnight(final @NotNull Date date) {
        final Calendar calendar = new GregorianCalendar();

        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTimeInMillis();
    }

    public record DayStatistic(@NotNull Date date,
                               int games,
                               float avgPlacement,
                               short tieCount,
                               float tieRate,
                               short winCount,
                               float winRate,
                               short topThreeCount,
                               float topThreeRate,
                               int walls,
                               float wallsPerWin,
                               short averageTime,
                               short fastestTime) {
    }

    public record DeathStatistic(@NotNull String reason, int count, int games) {

    }

    public record MapStatistic(@NotNull String map,
                               float averagePlacement,
                               String topDeathReason,
                               int ties,
                               int largestTie,
                               @NotNull String mostTiedWith,
                               int wins,
                               int topThrees,
                               int walls,
                               short averageTime,
                               short fastestTime) {

    }

    public record TieStatistic(@NotNull String player, int count) {

    }
}
