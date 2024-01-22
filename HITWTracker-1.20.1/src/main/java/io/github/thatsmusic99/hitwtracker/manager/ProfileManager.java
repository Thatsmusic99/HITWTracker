package io.github.thatsmusic99.hitwtracker.manager;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.thatsmusic99.hitwtracker.HITWTracker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ProfileManager {

    private static final @NotNull Logger LOGGER = LoggerFactory.getLogger(ProfileManager.class);
    private static final @NotNull String AVATAR_SERVICE = "https://cravatar.eu/helmavatar/%s/8";
    private final @NotNull HashMap<String, Identifier> idCache = new HashMap<>();
    private final @NotNull List<String> waiting = new ArrayList<>();


    public Identifier get(final @NotNull String name) {
        if (idCache.containsKey(name)) return idCache.get(name);

        if (!waiting.contains(name)) addToCache(name);

        return new Identifier("hitwtracker", "textures/steve.png");
    }

    private void addToCache(final @NotNull String name) {
        if (waiting.contains(name)) return;

        waiting.add(name);

        // Fetch from the file system
        CompletableFuture.runAsync(() -> {

            final File games = new File("games");
            if (!games.exists()) {
                if (!games.mkdirs()) {
                    LOGGER.error("Failed to create the games folder.");
                    return;
                }
            }
            final File cache = new File("cache");
            if (!cache.exists()) {
                if (!cache.mkdirs()) {
                    LOGGER.error("Failed to create the cache folder.");
                    return;
                }
            }

            // Attempt to find the file in the cache
            final File[] children = cache.listFiles();
            if (children != null) {
                for (File file : children) {
                    if (file.getName().equals(name + ".png")) {
                        load(cache, name);
                        return;
                    }
                }
            }

            // Otherwise, get the avatar from cravatar
            try (final InputStream avatar = getAvatar(name)) {

                byte[] data = avatar.readAllBytes();

                final File file = new File(cache, name + ".png");
                try (FileOutputStream stream = new FileOutputStream(file)) {
                    stream.write(data);
                }
            } catch (IOException ex) {
                LOGGER.error("Failed to fetch player icon " + name + " - " + ex.getMessage());
                return;
            }

           load(cache, name);

        }, HITWTracker.executorService).whenComplete((x, err) -> {
            if (err == null) return;
            LOGGER.error("Failed to fetch player icon " + name, err);
        });
    }

    private void load(final File cache, final String name) {
        try (FileInputStream stream = new FileInputStream(new File(cache, name + ".png"))) {
            final NativeImage image = NativeImage.read(stream);
            final NativeImageBackedTexture texture = new NativeImageBackedTexture(image);

            RenderSystem.recordRenderCall(() -> {
                TextureUtil.prepareImage(texture.getGlId(), image.getWidth(), image.getHeight());
                texture.upload();

                final Identifier id = new Identifier("hitwtracker", "player_" + name.toLowerCase());

                MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
                idCache.put(name, id);
                waiting.remove(name);
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private @NotNull InputStream getAvatar(final @NotNull String name) throws IOException {

        // Get the URL in question
        final URL url = new URL(String.format(AVATAR_SERVICE, name));
        final URLConnection connection = url.openConnection();

        return connection.getInputStream();
    }
}
