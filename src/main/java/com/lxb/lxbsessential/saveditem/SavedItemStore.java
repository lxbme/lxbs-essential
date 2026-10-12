package com.lxb.lxbsessential.saveditem;

import com.google.gson.reflect.TypeToken;
import com.lxb.lxbsessential.data.JsonIO;
import com.lxb.lxbsessential.data.Names;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The server-wide saved item library in {@code <root>/saved_items.json}. Every change is written at once;
 * if the write fails the change is undone, so memory never claims something the disk does not have.
 */
public final class SavedItemStore {

    private static final Logger LOGGER = LogManager.getLogger("lxbsessential");
    private static final Type TYPE = new TypeToken<LinkedHashMap<String, SavedItem>>() { }.getType();

    private final Path file;
    private LinkedHashMap<String, SavedItem> items = new LinkedHashMap<>();

    public SavedItemStore(Path root) {
        this.file = root.resolve("saved_items.json");
    }

    public void load() {
        JsonIO.Loaded<LinkedHashMap<String, SavedItem>> loaded =
                JsonIO.read(file, TYPE, LinkedHashMap::new, LocalDateTime.now());
        if (loaded.error != null) {
            LOGGER.error("Could not read {}, moved it to {} and started empty", file, loaded.quarantined, loaded.error);
        }
        items = normalize(loaded.value);
    }

    /** Re-keys hand-edited entries and drops ones that cannot be used. */
    private static LinkedHashMap<String, SavedItem> normalize(Map<String, SavedItem> raw) {
        LinkedHashMap<String, SavedItem> out = new LinkedHashMap<>();
        for (SavedItem entry : raw.values()) {
            if (entry == null || Names.validate(entry.name) != null) {
                LOGGER.warn("saved_items.json: dropping an entry with a missing or invalid name");
                continue;
            }
            if (entry.item == null || entry.item.isEmpty()) {
                LOGGER.warn("saved_items.json: dropping '{}' because it has no item", entry.name);
                continue;
            }
            out.put(Names.key(entry.name), entry);
        }
        return out;
    }

    @Nullable
    public SavedItem get(String name) {
        return items.get(Names.key(name));
    }

    public List<SavedItem> list() {
        List<SavedItem> list = new ArrayList<>(items.values());
        list.sort(Comparator.comparing(entry -> Names.key(entry.name)));
        return list;
    }

    /** Adds or replaces the entry with the same name; on a failed write the previous state is restored. */
    public void put(SavedItem item) throws IOException {
        String key = Names.key(item.name);
        SavedItem previous = items.put(key, item);
        try {
            save();
        } catch (IOException e) {
            if (previous == null) {
                items.remove(key);
            } else {
                items.put(key, previous);
            }
            throw e;
        }
    }

    /** False when there is no such entry; on a failed write the entry is put back. */
    public boolean remove(String name) throws IOException {
        String key = Names.key(name);
        SavedItem previous = items.remove(key);
        if (previous == null) {
            return false;
        }
        try {
            save();
        } catch (IOException e) {
            items.put(key, previous);
            throw e;
        }
        return true;
    }

    private void save() throws IOException {
        JsonIO.write(file, items);
    }
}
