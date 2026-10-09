package com.lxb.lxbsessential.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarpStoreTest {

    private static final Location L1 = new Location(0, 1.5, 64, -2.5, 90F, 10F);
    private static final Location L2 = new Location(-1, 10, 70, 10, 0F, 0F);

    @TempDir
    Path dir;

    @Test
    void putAndGetAreCaseInsensitive() {
        WarpStore store = new WarpStore(dir);
        store.load();
        assertFalse(store.put("Market", L1));
        assertEquals(L1, store.get("market").location);
        assertTrue(store.put("MARKET", L2));
        assertEquals("MARKET", store.get("Market").name);
        assertEquals(L2, store.get("market").location);
    }

    @Test
    void removeAndSortedList() {
        WarpStore store = new WarpStore(dir);
        store.load();
        store.put("b", L1);
        store.put("A", L1);
        store.put("c", L1);
        assertTrue(store.remove("C"));
        assertFalse(store.remove("c"));
        List<NamedLocation> list = store.list();
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).name);
        assertEquals("b", list.get(1).name);
    }

    @Test
    void saveAndReload() throws IOException {
        WarpStore first = new WarpStore(dir);
        first.load();
        first.put("Market", L1);
        first.save();
        assertTrue(Files.exists(dir.resolve("warps.json")));

        WarpStore second = new WarpStore(dir);
        second.load();
        assertEquals(L1, second.get("market").location);
    }

    @Test
    void loadMissingFileGivesEmpty() {
        WarpStore store = new WarpStore(dir);
        store.load();
        assertTrue(store.list().isEmpty());
        assertNull(store.get("anything"));
    }

    @Test
    void loadNormalizesHandEditedKeys() throws IOException {
        String json = "{\"X\":{\"name\":\"Market\",\"location\":{\"dim\":0,\"x\":1,\"y\":2,\"z\":3,\"yaw\":0,\"pitch\":0}}}";
        Files.write(dir.resolve("warps.json"), json.getBytes(StandardCharsets.UTF_8));
        WarpStore store = new WarpStore(dir);
        store.load();
        assertNotNull(store.get("market"));
        assertNull(store.get("x"));
    }
}
