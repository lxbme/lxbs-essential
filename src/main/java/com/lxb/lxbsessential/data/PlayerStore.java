package com.lxb.lxbsessential.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Per-player JSON files under {@code <root>/players/}, cached while the player is online. */
public final class PlayerStore {

    private static final Logger LOGGER = LogManager.getLogger("lxbsessential");

    private final Path dir;
    private final Map<UUID, PlayerData> cache = new HashMap<>();

    public PlayerStore(Path root) {
        this.dir = root.resolve("players");
    }

    public PlayerData get(UUID id) {
        PlayerData data = cache.get(id);
        if (data == null) {
            data = load(id);
            cache.put(id, data);
        }
        return data;
    }

    public boolean isLoaded(UUID id) {
        return cache.containsKey(id);
    }

    public void save(UUID id) throws IOException {
        PlayerData data = cache.get(id);
        if (data != null) {
            JsonIO.write(file(id), data);
        }
    }

    public void unload(UUID id) {
        cache.remove(id);
    }

    private PlayerData load(UUID id) {
        Path file = file(id);
        JsonIO.Loaded<PlayerData> loaded = JsonIO.read(file, PlayerData.class, PlayerData::new, LocalDateTime.now());
        if (loaded.error != null) {
            LOGGER.error("Could not read {}, moved it to {} and started empty", file, loaded.quarantined, loaded.error);
        }
        loaded.value.normalize();
        return loaded.value;
    }

    private Path file(UUID id) {
        return dir.resolve(id + ".json");
    }
}
