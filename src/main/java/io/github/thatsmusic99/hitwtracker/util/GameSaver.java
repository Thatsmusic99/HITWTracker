package io.github.thatsmusic99.hitwtracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.thatsmusic99.hitwtracker.game.Statistic;
import io.github.thatsmusic99.hitwtracker.serializer.StatisticSerializer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
        final String dateFormat = new SimpleDateFormat("dd-MM-yyyy").format(date);
        final File saveFile = new File(parentFolder, dateFormat + ".json");
        final File saveZipFile = new File(parentFolder, dateFormat + ".zip");

        final String content;

        // If the file doesn't exist, create it
        if (!saveZipFile.exists()) {
            if (!saveZipFile.createNewFile()) {
                throw new IOException("Failed to create file " + saveZipFile.getName());
            }
        }

        if (!saveFile.exists()) {
            if (!saveFile.createNewFile()) {
                throw new IOException("Failed to create file " + saveFile.getName());
            }
            content = getFileContents(saveZipFile);
        } else {
            content = getFileContents(saveFile);
        }

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

        try (final ZipOutputStream output = new ZipOutputStream(new FileOutputStream(saveZipFile))) {
            output.putNextEntry(new ZipEntry(saveFile.getName()));
            output.write(json.getBytes(StandardCharsets.UTF_8));
        }

        if (!saveFile.delete()) {
            LOGGER.warn("Failed to delete " + saveFile.getAbsolutePath());
        }

        LOGGER.info("Saved to " + saveZipFile.getAbsolutePath());
    }

    public static @NotNull String getFileContents(@NotNull File file) throws IOException {

        // See if it needs unzipping
        if (file.getName().endsWith(".zip")) {
            try (ZipInputStream stream = new ZipInputStream(new FileInputStream(file))) {
                ZipEntry entry = stream.getNextEntry();
                if (entry != null) {
                    return read(stream);
                }
            }
        }

        try (final InputStream reader = new BufferedInputStream(new FileInputStream(file))) {
            return read(reader);
        }
    }

    private static @NotNull String read(final @NotNull InputStream reader) throws IOException {

        StringBuilder content = new StringBuilder();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = reader.read(buffer)) > 0) {
            content.append(new String(buffer, 0, length));
        }

        // If there's nothing, then
        if (content.isEmpty()) content.append("[]");

        return content.toString();
    }

    private interface NoSuchFileConsumer<T> {

        T get() throws FileNotFoundException;
    }
}
