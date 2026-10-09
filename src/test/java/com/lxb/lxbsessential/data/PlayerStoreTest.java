package com.lxb.lxbsessential.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStoreTest {

    private static final UUID ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final Location L1 = new Location(0, 1.5, 64, -2.5, 90F, 10F);

    @TempDir
    Path dir;

    private Path playerFile() {
        return dir.resolve("players").resolve(ID + ".json");
    }

    @Test
    void getCachesSameInstance() {
        PlayerStore store = new PlayerStore(dir);
        assertSame(store.get(ID), store.get(ID));
        assertTrue(store.isLoaded(ID));
    }

    @Test
    void saveThenFreshStoreLoads() throws IOException {
        PlayerStore first = new PlayerStore(dir);
        first.get(ID).setHome("Base", L1, 3);
        first.save(ID);
        assertTrue(Files.exists(playerFile()));

        PlayerStore second = new PlayerStore(dir);
        assertEquals(L1, second.get(ID).getHome("base").location);
    }

    @Test
    void unloadDropsCache() {
        PlayerStore store = new PlayerStore(dir);
        store.get(ID);
        store.unload(ID);
        assertFalse(store.isLoaded(ID));
    }

    @Test
    void saveOfUnloadedPlayerWritesNothing() throws IOException {
        PlayerStore store = new PlayerStore(dir);
        store.save(ID);
        assertFalse(Files.exists(playerFile()));
    }

    @Test
    void loadsHandEditedFileWithMissingFields() throws IOException {
        Files.createDirectories(playerFile().getParent());
        Files.write(playerFile(), "{}".getBytes(StandardCharsets.UTF_8));

        PlayerData data = new PlayerStore(dir).get(ID);
        assertEquals(0, data.homeCount());
        assertNull(data.getBack());
        assertTrue(Files.exists(playerFile()));
    }

    @Test
    void corruptFileIsQuarantinedAndEmptyDataReturned() throws IOException {
        Files.createDirectories(playerFile().getParent());
        Files.write(playerFile(), "{oops".getBytes(StandardCharsets.UTF_8));

        PlayerData data = new PlayerStore(dir).get(ID);
        assertNotNull(data);
        assertEquals(0, data.homeCount());
        try (Stream<Path> files = Files.list(playerFile().getParent())) {
            assertTrue(files.anyMatch(p -> p.getFileName().toString().startsWith(ID + ".json.corrupt-")));
        }
    }
}
