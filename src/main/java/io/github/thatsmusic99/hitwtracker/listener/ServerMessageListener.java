package io.github.thatsmusic99.hitwtracker.listener;

import io.github.thatsmusic99.hitwtracker.game.GameTracker;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerMessageListener {

    private static final Logger logger = Logger.getLogger(ServerMessageListener.class.getName());
    private static final Pattern GAME_STARTED = Pattern.compile("^\\[.] Game Started!");
    private static final Pattern PLAYER_NAME = Pattern.compile("[^a-zA-Z_0-9]*(\\w+)[^a-zA-Z_0-9]*");
    private static final Pattern GAME_LOST = Pattern.compile("^\\[.] .+(\\w+), you were eliminated in (\\d+)\\w+ \\(Score: (\\d+).\\)");
    private static final Pattern GAME_WON = Pattern.compile("^\\[.] .+(\\w+), you survived the walls! \\(Score: (\\d+).\\)");
    private static final Pattern GAME_WINNERS = Pattern.compile("^\\[.] Game Winner\\(s\\): (.+)");
    private static final Pattern GAME_SURVIVED = Pattern.compile("\\s*.*Finished in ((\\d+)m)? ?((\\d+)s)?\\.");
    private static final Pattern GAME_DODGED = Pattern.compile("\\s*.*Dodged (\\d+) walls\\.");
    private static final Pattern GAME_TRAPS = Pattern.compile("\\s*.*Survived (\\d+) traps\\.");
    private static final Pattern GAME_PLAYERS = Pattern.compile("\\s*.*Outlived (\\d+) players.\\.");


    public static void checkMessage(String content) {

        // Game started
        if (GAME_STARTED.matcher(content).matches()) {
            GameTracker.start();
            return;
        }

        // Game lost
        Matcher matcher = GAME_LOST.matcher(content);
        if (matcher.matches()) {

            // Get the placement as an integer
            final byte placement = Byte.parseByte(matcher.group(2));
            final short score = Short.parseShort(matcher.group(3));

            // End the game and send the score
            GameTracker.end(placement);
            GameTracker.setScore(score);
            GameTracker.setTies();
            return;
        }

        // Game won by other players
        matcher = GAME_WINNERS.matcher(content);
        if (matcher.matches()) {

            // If we're not tracking, ignore
            if (!GameTracker.isTracking()) return;

            final String[] winners = getRawTies(matcher.group(1));
            GameTracker.setTies(winners);
            return;
        }

        // Game won
        matcher = GAME_WON.matcher(content);
        if (matcher.matches()) {

            // Get the score
            final short score = Short.parseShort(matcher.group(2));

            GameTracker.end((byte) 1);
            GameTracker.setScore(score);
            return;
        }

        // Survival duration
        matcher = GAME_SURVIVED.matcher(content);
        if (matcher.matches()) {

            // Get the minutes and seconds
            final byte minutes = matcher.group(2) == null ? 0 : Byte.parseByte(matcher.group(2));
            final byte seconds = matcher.group(4) == null ? 0 : Byte.parseByte(matcher.group(4));

            GameTracker.setDuration(minutes, seconds);
        }

        // Walls survived
        matcher = GAME_DODGED.matcher(content);
        if (matcher.matches()) {

            // Get the walls dodged
            final byte walls = Byte.parseByte(matcher.group(1));

            GameTracker.setWalls(walls);
        }

        // Trap activated
    }

    private static String[] getRawTies(String content) {

        // Store our own name because it's a faff to get
        final String self = MinecraftClient.getInstance().player == null ? "" : MinecraftClient.getInstance().player.getGameProfile().getName();

        // Split up names
        String[] rawNames = content.split(",");
        List<String> finalNames = new ArrayList<>();

        // For each name, strip extra details and get results
        for (String rawName : rawNames) {
            final var matcher = PLAYER_NAME.matcher(rawName);
            if (!matcher.matches()) continue;
            final String finalName = matcher.group(1);

            // Ensure it's not our own name, because that would be silly
            if (self.equals(finalName)) continue;
            finalNames.add(finalName);
        }

        // Return the result
        return finalNames.toArray(new String[0]);
    }
}
