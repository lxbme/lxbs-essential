package com.lxb.lxbsessential.info;

import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ShareCacheTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    private final AtomicLong now = new AtomicLong(1_000_000L);
    private final ShareCache<String> cache = new ShareCache<>(now::get);

    @Test
    void aSnapshotCanBeSharedOnce() {
        cache.put(A, "sword");

        assertEquals("sword", cache.take(A));
        assertNull(cache.take(A));
    }

    @Test
    void snapshotExpiresAfterFiveMinutes() {
        cache.put(A, "sword");
        now.addAndGet(ShareCache.TTL_MILLIS);
        assertEquals("sword", cache.take(A));

        cache.put(A, "bow");
        now.addAndGet(ShareCache.TTL_MILLIS + 1);
        assertNull(cache.take(A));
    }

    @Test
    void newerSnapshotReplacesOlder() {
        cache.put(A, "sword");
        cache.put(A, "bow");

        assertEquals("bow", cache.take(A));
        assertNull(cache.take(A));
    }

    @Test
    void playersAreIndependent() {
        cache.put(A, "sword");
        cache.put(B, "bow");

        assertEquals("bow", cache.take(B));
        assertEquals("sword", cache.take(A));
    }

    @Test
    void forgetAndClearDropSnapshots() {
        cache.put(A, "sword");
        cache.put(B, "bow");
        cache.forget(A);
        assertNull(cache.take(A));

        cache.clear();
        assertNull(cache.take(B));
    }
}
