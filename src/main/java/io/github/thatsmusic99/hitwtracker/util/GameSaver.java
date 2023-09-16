package io.github.thatsmusic99.hitwtracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.serializer.StatisticSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GameSaver {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameSaver.class);

    public static void saveGame(final @NotNull Statistic statistic) throws IOException {

        // Create the parent folder
        final File parentFolder = new File("games");
        if (!parentFolder.exists()) {
            if (!parentFolder.mkdir()) {
                throw new IOException("Failed to create parent folder " + parentFolder.getName());
            }
        }

        // Get the file to save to
        final Date date = statistic.date();
        final File saveFile = new File(parentFolder, new SimpleDateFormat("dd-MM-yyyy").format(date) + ".json");

        // If the file doesn't exist, create it
        if (!saveFile.exists()) {
            if (!saveFile.createNewFile()) {
                throw new IOException("Failed to create file " + saveFile.getName());
            }
        }

        // Read the file
        String content = getFileContents(saveFile);

        // Read as JSON - if all else fails, rename the file and create a new one
        final Gson gson = new GsonBuilder().registerTypeAdapter(Statistic.class, new StatisticSerializer()).create();
        final Statistic[] existingStats = gson.fromJson(content, Statistic[].class);
        final Statistic[] newStats = new Statistic[existingStats.length + 1];

        System.arraycopy(existingStats, 0, newStats, 0, existingStats.length);
        newStats[existingStats.length] = statistic;

        // Save to JSON
        final String json = gson.toJson(newStats);
        try (final BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            writer.write(json);
        }

        LOGGER.info("Saved to " + saveFile.getAbsolutePath());
    }

    public static @NotNull String getFileContents(@NotNull File file) throws IOException {

        // Read the file
        StringBuilder content = new StringBuilder();
        try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }
        }

        // If there's nothing, then
        if (content.isEmpty()) content.append("[]");

        return content.toString();
    }
}
