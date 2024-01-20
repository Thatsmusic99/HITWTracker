package io.github.thatsmusic99.hitwtracker.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import io.github.thatsmusic99.hitwtracker.HITWTracker;
import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.serializer.StatisticSerializer;
import io.github.thatsmusic99.hitwtracker.util.GameSaver;
import io.github.thatsmusic99.hitwtracker.util.MiscUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StatisticManager {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(StatisticManager.class);
    private final @NotNull HashMap<Long, List<Statistic>> dailyStats;
    private final @NotNull HashMap<Long, DayStatistic> dayStatistics;
    private final @NotNull List<DeathStatistic> deathStatistics;
    private final @NotNull HashMap<String, MapStatistic> mapStatistics;
    private final @NotNull List<MiscStatistic> miscStatistics;
    private final @NotNull List<TieStatistic> tieStatistics;
    private final @NotNull List<Long> times;
    private transient @Nullable CompletableFuture<Void> loading;
    private boolean loaded;

    public StatisticManager() {
        this.dailyStats = new HashMap<>();
        this.dayStatistics = new HashMap<>();
        this.deathStatistics = new ArrayList<>();
        this.mapStatistics = new HashMap<>();
        this.miscStatistics = new ArrayList<>();
        this.tieStatistics = new ArrayList<>();
        this.times = new ArrayList<>();
        this.loaded = false;
    }

    private @NotNull CompletableFuture<Void> load() {
        if (loading != null) return loading;
        LOGGER.info("Loading statistic data...");

        return loading = CompletableFuture.runAsync(() -> {

            // Go through each file in the games folder
            final File gamesFolder = new File("games");
            if (!gamesFolder.exists()) {
                LOGGER.info("Games folder does not exist.");
                loading = null;
                return;
            }

            final File hitwFolder = new File(gamesFolder, "hitw");
            if (!hitwFolder.exists()) {
                LOGGER.info("HITW folder does not exist.");
                loading = null;
                return;
            }

            // Go through each file
            final File[] files = hitwFolder.listFiles();
            if (files == null || files.length == 0) {
                LOGGER.info("Folder is empty.");
                loading = null;
                return;
            }
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
            LOGGER.info("Loaded " + this.dailyStats.size() + " stats.");
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
                final var stat = formDayStatistics(time);
                if (stat == null) continue;
                stats.put(time, stat);
            }

            return stats;
        });
    }

    public @NotNull CompletableFuture<@NotNull List<DeathStatistic>> getDeathStats() {
        return getStats(this.deathStatistics, this::formDeathStatistics);
    }

    public @NotNull CompletableFuture<@NotNull HashMap<String, MapStatistic>> getMapStats() {
        return getStats(this.mapStatistics, this::formMapStatistics);
    }

    public @NotNull CompletableFuture<@NotNull List<MiscStatistic>> getMiscStats() {
        return getStats(this.miscStatistics, this::formMiscStatistics);
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

        int games = stats.size();

        int placements = 0;
        short ties = 0;
        short wins = 0;
        short topThrees = 0;
        int walls = 0;
        short totalTime = 0;
        short shortestTime = 240;

        // Go through each statistic...
        for (Statistic stat : stats) {
            if (stat.plobby()) {
                games--;
                continue;
            }
            placements += stat.placement();
            if (stat.ties().length > 0) ties++;
            if (stat.placement() == 1) wins++;
            if (stat.placement() <= 3) topThrees++;
            walls += stat.walls();
            totalTime += stat.seconds();
            shortestTime = stat.placement() == 1 ? (short) Math.min(shortestTime, stat.seconds()) : shortestTime;
        }

        if (games == 0) return null;

        final float avgPlace = placements / (float) games;

        return new DayStatistic(date, games, avgPlace, ties, wins, topThrees, walls, totalTime, shortestTime);

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

    private @NotNull HashMap<String, MapStatistic> formMapStatistics() {

        // Get all statistics
        for (long time : this.dailyStats.keySet()) {
            for (Statistic statistic : this.dailyStats.get(time)) {
                if (statistic.plobby()) continue;

                // Overall stats
                if (this.mapStatistics.containsKey("OVERALL")) {
                    this.mapStatistics.get("OVERALL").update(statistic);
                } else {
                    this.mapStatistics.put("OVERALL", new MapStatistic("Overall"));
                }

                if (this.mapStatistics.containsKey(statistic.map())) {
                    this.mapStatistics.get(statistic.map()).update(statistic);
                } else {
                    this.mapStatistics.put(statistic.map(), new MapStatistic(statistic.map()));
                }
            }
        }

        return this.mapStatistics;
    }

    private @NotNull List<MiscStatistic> formMiscStatistics() {

        // Highest streak stats (ties, wins, top 3's)
        int[] streaks = new int[3];

        // Daily records (ties, wins, top 3's)
        int[] dailyRecords = new int[3];

        // Average stats
        int totalGames = 0;
        int totalWins = 0;
        int totalTime = 0;

        for (long time : this.dailyStats.keySet()) {

            int tieCount = 0;
            int winCount = 0;
            int topThreeCount = 0;

            int[] dayStreaks = new int[3];

            // Find each streak
            for (Statistic statistic : this.dailyStats.get(time)) {
                if (statistic.plobby()) continue;

                if (statistic.ties().length > 0) {
                    tieCount++;
                    dayStreaks[0]++;
                } else {
                    dayStreaks[0] = 0;
                }

                if (statistic.placement() == 1) {
                    winCount++;
                    dayStreaks[1]++;
                } else {
                    dayStreaks[1] = 0;
                }

                if (statistic.placement() < 4) {
                    topThreeCount++;
                    dayStreaks[2]++;
                } else {
                    dayStreaks[2] = 0;
                }

                streaks[0] = Math.max(dayStreaks[0], streaks[0]);
                streaks[1] = Math.max(dayStreaks[1], streaks[1]);
                streaks[2] = Math.max(dayStreaks[2], streaks[2]);

                totalTime += statistic.seconds();
                totalGames++;
            }

            dailyRecords[0] = Math.max(dailyRecords[0], tieCount);
            dailyRecords[1] = Math.max(dailyRecords[1], winCount);
            dailyRecords[2] = Math.max(dailyRecords[2], topThreeCount);

            totalWins += winCount;
        }

        //
        final List<MiscStatistic> stats = new ArrayList<>();
        stats.add(new MiscStatistic("Highest Tie Streak", String.valueOf(streaks[0])));
        stats.add(new MiscStatistic("Highest Win Streak", String.valueOf(streaks[1])));
        stats.add(new MiscStatistic("Highest Top Three Streak", String.valueOf(streaks[2])));

        stats.add(new MiscStatistic("Daily Tie Record", String.valueOf(dailyRecords[0])));
        stats.add(new MiscStatistic("Daily Win Record", String.valueOf(dailyRecords[1])));
        stats.add(new MiscStatistic("Daily Top Three Record", String.valueOf(dailyRecords[2])));

        stats.add(new MiscStatistic("Average Games per Day", String.valueOf(totalGames / Math.max(1, this.dailyStats.size()))));
        stats.add(new MiscStatistic("Average Wins per Day", String.valueOf(totalWins / Math.max(1, this.dailyStats.size()))));
        stats.add(new MiscStatistic("Average Time per Day", toTimeUnits(totalTime / Math.max(1, this.dailyStats.size()))));

        stats.add(new MiscStatistic("Total Time In-Game", toTimeUnits(totalTime)));

        this.miscStatistics.addAll(stats);

        return stats;
    }

    private @NotNull List<TieStatistic> formTieStatistics() {

        // Go through each statistic for each day
        HashMap<String, List<String>> ties = new HashMap<>();
        for (long time : this.dailyStats.keySet()) {
            for (Statistic stat : this.dailyStats.get(time)) {
                // TODO - distinguish between plobby and normal ties
                // if (stat.plobby()) continue;

                // Go through each name
                for (String name : stat.ties()) {
                    final String username = getUsername(name);
                    final var list = ties.getOrDefault(username, new ArrayList<>());
                    list.add(stat.map());
                    ties.put(username, list);
                }
            }
        }

        // Go through each name and create tie stat records
        List<TieStatistic> tieStats = new ArrayList<>();
        for (String name : ties.keySet()) {
            final var stat = new TieStatistic(name, ties.get(name).size());
            tieStats.add(stat);

            for (String map : ties.get(name)) {
                stat.add(map);
            }
        }

        this.tieStatistics.addAll(tieStats);

        return tieStats;
    }

    public void addStatToCache(final @NotNull Statistic statistic) {

        // If stats haven't already been loaded, don't for now
        if (!loaded) {
            LOGGER.info("Statistics are not loaded, not adding the stat to cache.");
            return;
        }

        // Get the date to save to
        long time = getDayAtMidnight(statistic.date());

        final List<Statistic> existingStats = this.dailyStats.computeIfAbsent(time, x -> {
            this.times.add(x);
            return new ArrayList<>();
        });

        existingStats.add(statistic);
        this.dailyStats.put(time, existingStats);
        updateTieStatistic(statistic);

        if (statistic.plobby()) return;

        updateDayStatistic(statistic);
        updateDeathStatistic(statistic);
        updateMapStatistic(statistic);
    }

    private void updateDayStatistic(final @NotNull Statistic statistic) {

        // Get the date to save to
        long time = getDayAtMidnight(statistic.date());

        // Update day statistic
        DayStatistic dayStatistic = this.dayStatistics.get(time);
        if (dayStatistic != null) {

            final Date date = dayStatistic.date;
            final int games = dayStatistic.games + 1;
            final float avgPlacement = (dayStatistic.games * dayStatistic.avgPlacement + statistic.placement()) / dayStatistic.games;
            final short tieCount = (short) (statistic.ties().length > 0 ? dayStatistic.tieCount + 1 : dayStatistic.tieCount);
            final short winCount = (short) (statistic.placement() == 1 ? dayStatistic.winCount + 1 : dayStatistic.winCount);
            final short topThreeCount = (short) (statistic.placement() < 4 ? dayStatistic.topThreeCount + 1 : dayStatistic.topThreeCount);
            final int walls = dayStatistic.walls + statistic.walls();
            final short totalTime = (short) (dayStatistic.totalTime + statistic.seconds());
            final short shortestTime = (short) (statistic.placement() == 1 ? Math.min(statistic.seconds(), dayStatistic.fastestWin) : dayStatistic.fastestWin);

            this.dayStatistics.put(time, new DayStatistic(date, games, avgPlacement, tieCount, winCount, topThreeCount,
                    walls, totalTime, shortestTime));
        }
    }

    private void updateDeathStatistic(final @NotNull Statistic statistic) {

        // If there's no death reason, stop there
        if (statistic.deathCause().isEmpty()) return;

        // Go through each death statistic and update stats
        for (DeathStatistic deathStatistic : this.deathStatistics) {
            deathStatistic.games++;
            if (deathStatistic.reason.equals(statistic.deathCause())) {
                deathStatistic.count++;
            }
        }
    }

    private void updateMapStatistic(final @NotNull Statistic statistic) {
        if (this.mapStatistics.containsKey(statistic.map())) {
            this.mapStatistics.get(statistic.map()).update(statistic);
        }

        if (this.mapStatistics.containsKey("OVERALL")) {
            this.mapStatistics.get("OVERALL").update(statistic);
        }
    }

    private void updateTieStatistic(final @NotNull Statistic statistic) {

        // If there's no ties, stop there
        if (statistic.ties().length == 0) return;

        // Go through each tie to update
        List<String> tiedPlayersAdded = new ArrayList<>(Arrays.asList(statistic.ties()));
        for (TieStatistic tieStatistic : this.tieStatistics) {
            for (String player : statistic.ties()) {
                if (tieStatistic.player.equals(player)) {
                    tieStatistic.count++;
                    tieStatistic.add(statistic.map());
                    tiedPlayersAdded.remove(player);
                }
            }
        }

        // For each player not added, add them
        for (String player : tiedPlayersAdded) {
            this.tieStatistics.add(new TieStatistic(player, 1));
        }
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

    private String toTimeUnits(int seconds) {
        if (seconds < 60) {
            return seconds + "s";
        }
        if (seconds < 3600) {
            int remainingSecs = (seconds % 60);
            return (seconds / 60) + "m" + (remainingSecs == 0 ? "" : " " + remainingSecs + "s");
        }
        int remainingSecs = (seconds % 60);
        int remainingMinutes = (seconds % 3600) / 60;
        int remainingHours = seconds / 3600;
        return remainingHours + "hr"
                + (remainingMinutes == 0 ? "" : " " + remainingMinutes + "m")
                + (remainingSecs == 0 ? "" : " " + remainingSecs + "s");
    }

    public record DayStatistic(@NotNull Date date,
                               int games,
                               float avgPlacement,
                               short tieCount,
                               short winCount,
                               short topThreeCount,
                               int walls,
                               short totalTime,
                               short fastestWin) {

        public float tieRate() {
            return tieCount / (float) games;
        }

        public float winRate() {
            return winCount / (float) games;
        }

        public float topThreeRate() {
            return topThreeCount / (float) games;
        }

        public float wallsPerWin() {
            return walls / (float) winCount;
        }

        public short averageTime() {
            return (short) (totalTime / games);
        }
    }

    public static class DeathStatistic {

        private final @NotNull String reason;
        private int count;
        private int games;

        public DeathStatistic(@NotNull String reason, int count, int games) {
            this.reason = reason;
            this.count = count;
            this.games = games;
        }

        public int getGames() {
            return games;
        }

        public int getCount() {
            return count;
        }

        public @NotNull String getReason() {
            return reason;
        }
    }

    public static class MapStatistic {

        private final @NotNull String map;
        private int games;
        private float avgPlacement;
        private final @NotNull List<String> deaths;
        private @Nullable String topDeathCause;
        private int ties;
        private int largestTie;
        private final @NotNull List<String> tiedWith;
        private @Nullable String mostTiedWith;
        private int wins;
        private int topThrees;
        private int walls;
        private short averageTime;
        private short fastestTime;

        public MapStatistic(final @NotNull String map) {
            this(map, 0, 0, new ArrayList<>(), 0, 0, new ArrayList<>(), 0,
                    0, 0, (short) 0, (short) 240);
        }

        public MapStatistic(final @NotNull String map,
                            int games,
                            float avgPlacement,
                            @NotNull List<String> deaths,
                            int ties,
                            int largestTie,
                            @NotNull List<String> tiedWith,
                            int wins,
                            int topThrees,
                            int walls,
                            short averageTime,
                            short fastestTime) {
            this.map = MiscUtils.capitalise(map);
            this.games = games;
            this.avgPlacement = avgPlacement;
            this.deaths = deaths;
            this.topDeathCause = getTopResult(deaths);
            this.ties = ties;
            this.largestTie = largestTie;
            this.tiedWith = tiedWith;
            this.mostTiedWith = getUsername(getTopResult(tiedWith));
            this.wins = wins;
            this.topThrees = topThrees;
            this.walls = walls;
            this.averageTime = averageTime;
            this.fastestTime = fastestTime;
        }

        void update(Statistic statistic) {

            this.games++;
            this.avgPlacement = ((this.avgPlacement * (this.games - 1)) + statistic.placement()) / (float) this.games;
            this.deaths.add(statistic.deathCause());
            this.topDeathCause = getTopResult(this.deaths);
            if (statistic.ties().length > 0) this.ties++;
            this.largestTie = Math.max(this.largestTie, statistic.ties().length);
            this.tiedWith.addAll(Arrays.asList(statistic.ties()));
            this.mostTiedWith = getUsername(getTopResult(this.tiedWith));
            if (statistic.placement() == 1) this.wins++;
            if (statistic.placement() < 4) this.topThrees++;
            this.walls += statistic.walls();
            this.averageTime = (short) (((this.averageTime * (this.games - 1)) + statistic.seconds()) / this.games);
            if (statistic.placement() == 1) this.fastestTime = (short) Math.min(this.fastestTime, statistic.seconds());
        }

        private @Nullable String getTopResult(@NotNull List<String> list) {

            HashMap<String, Integer> counts = new HashMap<>();
            int maxValue = 0;
            String maxResult = null;

            for (String str : list) {
                if (str.isEmpty()) continue;
                int count = 1;
                if (counts.containsKey(str)) {
                    count = counts.get(str) + 1;
                } else {
                    counts.put(str, count);
                }

                if (maxValue < count) {
                    maxValue = count;
                    maxResult = str;
                }
            }

            return maxResult;
        }

        public @NotNull String getMap() {
            return map;
        }

        public int getGames() {
            return games;
        }

        public float getAvgPlacement() {
            return avgPlacement;
        }

        public @Nullable String getTopDeathCause() {
            return topDeathCause;
        }

        public int getTies() {
            return ties;
        }

        public int getLargestTie() {
            return largestTie;
        }

        public @Nullable String getMostTiedWith() {
            return mostTiedWith;
        }

        public int getWins() {
            return wins;
        }

        public int getTopThrees() {
            return topThrees;
        }

        public int getWalls() {
            return walls;
        }

        public short getAverageTime() {
            return averageTime;
        }

        public short getFastestTime() {
            return fastestTime;
        }
    }

    public record MiscStatistic(@NotNull String descriptor, @NotNull String value) {}

    public static class TieStatistic {

        private final @NotNull String player;
        private int count;
        private final HashMap<String, Integer> maps;

        public TieStatistic(@NotNull String player, int count) {
            this.player = getUsername(player);
            this.count = count;
            this.maps = new HashMap<>();
        }

        public @NotNull String getPlayer() {
            return player;
        }

        public int getCount() {
            return count;
        }

        public int getMapCount(final @NotNull String map) {
            return this.maps.getOrDefault(map.toLowerCase(), 0);
        }

        public Set<String> getTiedMaps() {
            return this.maps.keySet();
        }

        protected void add(final @NotNull String map) {
            int count = getMapCount(map);
            count++;
            this.maps.put(map.toLowerCase(), count);
        }
    }

    public static @NotNull String getUsername(final @Nullable String player) {
        if (player == null) return "N/A";
        final UUID uuid;
        try {
            uuid = UUID.fromString(player);
        } catch (IllegalArgumentException ex) {
            return player;
        }

        final Optional<GameProfile> profile = HITWTracker.get().getUserCache().getByUuid(uuid);
        if (profile.isEmpty()) return player;

        return profile.get().getName();
    }
}
