package com.lxb.lxbsessential.teleport;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** In-memory teleport cooldowns; they intentionally reset when the server restarts. */
public final class Cooldowns {

    private final Map<UUID, Long> until = new HashMap<>();

    public void start(UUID id, long nowMillis, int seconds) {
        if (seconds > 0) {
            until.put(id, nowMillis + seconds * 1000L);
        }
    }

    public long remainingMillis(UUID id, long nowMillis) {
        Long end = until.get(id);
        if (end == null) {
            return 0;
        }
        long remaining = end - nowMillis;
        if (remaining <= 0) {
            until.remove(id);
            return 0;
        }
        return remaining;
    }

    public void clear() {
        until.clear();
    }

    public static int toDisplaySeconds(long millis) {
        return (int) ((millis + 999) / 1000);
    }
}
