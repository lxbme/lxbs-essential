package com.lxb.lxbsessential.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonIOTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 9, 12, 34, 56);
    private static final Location L1 = new Location(0, 1.5, 64, -2.5, 90F, 10F);
    private static final Location L2 = new Location(-1, 10, 70, 10, 0F, 0F);

    @TempDir
    Path dir;

    private static void writeRaw(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void missingFileReturnsEmpty() {
        JsonIO.Loaded<PlayerData> loaded = JsonIO.read(dir.resolve("none.json"), PlayerData.class, PlayerData::new, NOW);
        assertNotNull(loaded.value);
        assertNull(loaded.quarantined);
        assertNull(loaded.error);
    }

    @Test
    void roundTrip() throws IOException {
        Path file = dir.resolve("p.json");
        PlayerData data = new PlayerData();
        data.setHome("Base", L1, 3);
        data.setBack(L2);
        JsonIO.write(file, data);

        JsonIO.Loaded<PlayerData> loaded = JsonIO.read(file, PlayerData.class, PlayerData::new, NOW);
        assertNull(loaded.error);
        assertEquals(L1, loaded.value.getHome("base").location);
        assertEquals("Base", loaded.value.getHome("base").name);
        assertEquals(L2, loaded.value.getBack());
    }

    @Test
    void writeCreatesParentDirectoriesAndLeavesNoTmp() throws IOException {
        Path file = dir.resolve("a").resolve("b").resolve("c.json");
        JsonIO.write(file, new PlayerData());
        assertTrue(Files.exists(file));
        assertFalse(Files.exists(dir.resolve("a").resolve("b").resolve("c.json.tmp")));
    }

    @Test
    void staleTmpFileIsOverwritten() throws IOException {
        Path file = dir.resolve("p.json");
        writeRaw(dir.resolve("p.json.tmp"), "{garbage from a crash");
        PlayerData data = new PlayerData();
        data.setHome("home", L1, 3);
        JsonIO.write(file, data);

        JsonIO.Loaded<PlayerData> loaded = JsonIO.read(file, PlayerData.class, PlayerData::new, NOW);
        assertNull(loaded.error);
        assertEquals(L1, loaded.value.getHome("home").location);
        assertFalse(Files.exists(dir.resolve("p.json.tmp")));
    }

    @Test
    void corruptFileIsQuarantined() throws IOException {
        Path file = dir.resolve("p.json");
        writeRaw(file, "{not json");

        JsonIO.Loaded<PlayerData> loaded = JsonIO.read(file, PlayerData.class, PlayerData::new, NOW);
        assertNotNull(loaded.value);
        assertNotNull(loaded.error);
        assertEquals(dir.resolve("p.json.corrupt-20261009-123456"), loaded.quarantined);
        assertTrue(Files.exists(loaded.quarantined));
        assertFalse(Files.exists(file));
    }

    @Test
    void wrongShapeIsQuarantined() throws IOException {
        Path file = dir.resolve("p.json");
        writeRaw(file, "[1, 2, 3]");

        JsonIO.Loaded<PlayerData> loaded = JsonIO.read(file, PlayerData.class, PlayerData::new, NOW);
        assertNotNull(loaded.value);
        assertNotNull(loaded.error);
        assertNotNull(loaded.quarantined);
    }

    @Test
    void emptyFileGivesEmptyValueWithoutQuarantine() throws IOException {
        Path file = dir.resolve("p.json");
        writeRaw(file, "");

        JsonIO.Loaded<PlayerData> loaded = JsonIO.read(file, PlayerData.class, PlayerData::new, NOW);
        assertNotNull(loaded.value);
        assertNull(loaded.error);
        assertNull(loaded.quarantined);
        assertTrue(Files.exists(file));
    }
}
