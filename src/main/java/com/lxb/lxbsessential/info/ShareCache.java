package com.lxb.lxbsessential.info;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;

/**
 * The last /info report of each player, kept in memory so [Share] broadcasts what was shown rather than
 * what is in hand at click time. Each snapshot can be shared once, which keeps [Share] from spamming chat.
 */
public final class ShareCache<T> {

    public static final long TTL_MILLIS = 5 * 60 * 1000L;
    public static final ShareCache<ItemReport> REPORTS = new ShareCache<>(System::currentTimeMillis);

    private final LongSupplier clock;
    private final Map<UUID, Entry<T>> entries = new HashMap<>();

    public ShareCache(LongSupplier clock) {
        this.clock = clock;
    }

    public void put(UUID id, T value) {
        entries.put(id, new Entry<>(value, clock.getAsLong()));
    }

    /** The snapshot, removed so it cannot be shared again; null when missing or older than five minutes. */
    @Nullable
    public T take(UUID id) {
        Entry<T> entry = entries.remove(id);
        if (entry == null || clock.getAsLong() - entry.createdAt > TTL_MILLIS) {
            return null;
        }
        return entry.value;
    }

    public void forget(UUID id) {
        entries.remove(id);
    }

    public void clear() {
        entries.clear();
    }

    private static final class Entry<T> {
        final T value;
        final long createdAt;

        Entry(T value, long createdAt) {
            this.value = value;
            this.createdAt = createdAt;
        }
    }
}
