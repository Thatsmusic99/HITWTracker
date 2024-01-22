package io.github.thatsmusic99.hitwtracker.game;

import io.github.thatsmusic99.hitwtracker.util.interfaces.DamageTracking;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

/**
 * The class used to keep track of game information before the game is confirmed or started.
 *
 * The process is generally as follows:
 * - Receive map from team prefix
 * - Confirm title through scoreboard name
 * - Start game
 */
public class GameTracker {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameTracker.class);
    public static final Function<Short, Byte> SECONDS_TO_WALLS = seconds -> (byte) (0.135 * seconds);
    // private static final Function<Integer, Integer> SECONDS_TO_TRAPS = seconds -> seconds == 240 ? 24 :

    private static @NotNull State state = State.WAITING;
    private static @Nullable String map;
    private static @Nullable String lastTrap;
    private static @Nullable Game game;
    private static boolean plobby = false;

    public static int startingY = 65;

    public static void setMap(@NotNull String map) {
        if (state == State.GAME_REJECTED) return;
        GameTracker.map = map;
        if (state == State.MAP_WAITING) state = State.GAME_CONFIRMED;
    }

    public static void confirm(boolean plobby, int startingY) {
        state = map == null ? State.MAP_WAITING : State.GAME_CONFIRMED;
        LOGGER.info("Game confirmed, state: " + state);

        GameTracker.startingY = startingY;
        GameTracker.plobby = plobby;
    }

    public static void reject() {
        state = State.GAME_REJECTED;
        LOGGER.info("Game rejected");
    }

    public static void start(final @NotNull DamageTracking<?> player) {
        if (state == State.GAME_REJECTED) return;
        game = new Game(map, player);
        game.start();
        game.setPlobby(plobby);
        LOGGER.info("Game started! Map: " + map);
        state = State.GAME_STARTED;
    }

    public static void win(int score) {

    }

    public static boolean isTracking() {
        return state == State.GAME_STARTED;
    }

    public static void end(byte placement) {
        if (game == null) return;
        state = placement == 1 ? State.GAME_WON : State.GAME_LOST;
        game.end();
        game.setPlacement(placement);
        LOGGER.info("Game ended, placed at " + placement);
    }

    public static void setTies(String... ties) {
        if (game == null) return;
        if (state == State.GAME_LOST) return;

        game.setTies(ties);
    }

    public static void setScore(short score) {
        if (game == null) return;
        game.setScore(score);
    }

    public static void setDuration(byte minutes, byte seconds) {
        if (game == null) return;
        game.setDuration((short) (minutes * 60 + seconds));
    }

    public static void setWalls(byte walls) {
        if (game == null) return;
        game.setWalls(walls);
    }

    public static void reset() {

        // Don't save if the game didn't even finish
        if (state != State.GAME_STARTED) {
            if (game != null) {
                game.save().whenComplete((v, err) -> {
                    if (err != null) {
                        LOGGER.error("Failed to save game: " + err.getCause().getMessage());
                    } else {
                        LOGGER.info("Successfully saved game.");
                    }
                });
            }
        }
        map = null;
        state = State.WAITING;
        game = null;
    }
}
