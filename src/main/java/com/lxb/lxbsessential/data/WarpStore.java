package com.lxb.lxbsessential.data;

import com.google.gson.reflect.TypeToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;

/** Server-wide warps stored in {@code <root>/warps.json}. */
public final class WarpStore {

    private static final Logger LOGGER = LogManager.getLogger("lxbsessential");
    private static final Type TYPE = new TypeToken<LinkedHashMap<String, NamedLocation>>() { }.getType();

    private final Path file;
    private LinkedHashMap<String, NamedLocation> warps = new LinkedHashMap<>();

    public WarpStore(Path root) {
        this.file = root.resolve("warps.json");
    }

    public void load() {
        JsonIO.Loaded<LinkedHashMap<String, NamedLocation>> loaded =
                JsonIO.read(file, TYPE, LinkedHashMap::new, LocalDateTime.now());
        if (loaded.error != null) {
            LOGGER.error("Could not read {}, moved it to {} and started empty", file, loaded.quarantined, loaded.error);
        }
        warps = NamedLocation.normalize(loaded.value);
    }

    public NamedLocation get(String name) {
        return warps.get(Names.key(name));
    }

    /** Returns true when an existing warp was overwritten. */
    public boolean put(String name, Location location) {
        return warps.put(Names.key(name), new NamedLocation(name, location)) != null;
    }

    public boolean remove(String name) {
        return warps.remove(Names.key(name)) != null;
    }

    public List<NamedLocation> list() {
        return NamedLocation.sorted(warps.values());
    }

    public void save() throws IOException {
        JsonIO.write(file, warps);
    }
}
