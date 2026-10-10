package com.lxb.lxbsessential.death;

import com.lxb.lxbsessential.data.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Each player's most recent death, kept in memory only: the respawn notice is handed out once,
 * and the location can be broadcast once per death so [Share] cannot be used to spam chat.
 */
public final class DeathLog {

    public static final DeathLog INSTANCE = new DeathLog();

    private final Map<UUID, Entry> entries = new HashMap<>();

    private static final class Entry {
        final Location location;
        boolean noticePending = true;
        boolean shared;

        Entry(Location location) {
            this.location = location;
        }
    }

    public void record(UUID id, Location location) {
        entries.put(id, new Entry(location));
    }

    public boolean hasDeath(UUID id) {
        return entries.containsKey(id);
    }

    /** The death location the first time it is asked for after a death, otherwise null. */
    public Location takeNotice(UUID id) {
        Entry entry = entries.get(id);
        if (entry == null || !entry.noticePending) {
            return null;
        }
        entry.noticePending = false;
        return entry.location;
    }

    /** The death location if it has not been shared yet, otherwise null. */
    public Location share(UUID id) {
        Entry entry = entries.get(id);
        if (entry == null || entry.shared) {
            return null;
        }
        entry.shared = true;
        return entry.location;
    }

    public void forget(UUID id) {
        entries.remove(id);
    }

    public void clear() {
        entries.clear();
    }
}
