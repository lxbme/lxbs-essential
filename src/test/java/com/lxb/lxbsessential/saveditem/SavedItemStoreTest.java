package com.lxb.lxbsessential.saveditem;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedItemStoreTest {

    @TempDir
    Path dir;

    private static SavedItem item(String name, String snbt) {
        return new SavedItem(name, snbt, "LxbThh", "d3413a9e-45b1-305c-9152-7dbeeb94de2d", "2026-10-12T10:00:00");
    }

    private SavedItemStore loaded() {
        SavedItemStore store = new SavedItemStore(dir);
        store.load();
        return store;
    }

    /** A directory where the file should be makes every write fail, like a full or read-only disk. */
    private void breakWrites() throws IOException {
        Path file = dir.resolve("saved_items.json");
        Files.deleteIfExists(file);
        Files.createDirectories(file);
        Files.createFile(file.resolve("blocker"));
    }

    @Test
    void lookupIsCaseInsensitiveAndKeepsTheSpelling() throws IOException {
        SavedItemStore store = loaded();
        store.put(item("Starter_Sword", "{a}"));

        assertEquals("Starter_Sword", store.get("starter_sword").name);
        assertEquals("{a}", store.get("STARTER_SWORD").item);
        assertNull(store.get("other"));
    }

    @Test
    void listIsSortedByName() throws IOException {
        SavedItemStore store = loaded();
        store.put(item("b", "{}"));
        store.put(item("A", "{}"));
        store.put(item("c", "{}"));
        assertTrue(store.remove("C"));
        assertFalse(store.remove("c"));

        List<SavedItem> list = store.list();
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).name);
        assertEquals("b", list.get(1).name);
    }

    @Test
    void entriesSurviveAReload() throws IOException {
        loaded().put(item("Starter", "{id:\"minecraft:stick\",Count:1b,Damage:0s}"));

        SavedItem back = loaded().get("starter");
        assertNotNull(back);
        assertEquals("Starter", back.name);
        assertEquals("{id:\"minecraft:stick\",Count:1b,Damage:0s}", back.item);
        assertEquals("LxbThh", back.savedBy);
        assertEquals("d3413a9e-45b1-305c-9152-7dbeeb94de2d", back.savedById);
        assertEquals("2026-10-12T10:00:00", back.savedAt);
    }

    @Test
    void corruptFileIsMovedAsideAndTheLibraryStartsEmpty() throws IOException {
        Files.write(dir.resolve("saved_items.json"), "{not json".getBytes(StandardCharsets.UTF_8));

        assertTrue(loaded().list().isEmpty());
        try (Stream<Path> files = Files.list(dir)) {
            assertTrue(files.anyMatch(p -> p.getFileName().toString().startsWith("saved_items.json.corrupt-")));
        }
    }

    @Test
    void failedWriteRollsBackANewEntry() throws IOException {
        SavedItemStore store = loaded();
        breakWrites();

        assertThrows(IOException.class, () -> store.put(item("Sword", "{a}")));
        assertNull(store.get("sword"));
    }

    @Test
    void failedWriteRollsBackUpdateAndRemove() throws IOException {
        SavedItemStore store = loaded();
        store.put(item("Sword", "{a}"));
        breakWrites();

        assertThrows(IOException.class, () -> store.put(item("Sword", "{b}")));
        assertEquals("{a}", store.get("sword").item);
        assertThrows(IOException.class, () -> store.remove("SWORD"));
        assertEquals("{a}", store.get("sword").item);
    }

    /**
     * Gson reports an encoder failure mid-write as an unchecked JsonIOException; it must still roll back.
     * A lone UTF-16 surrogate cannot be written as UTF-8, so it fails inside Gson like a full disk would.
     */
    @Test
    void failureInsideTheJsonWriterRollsBackToo() throws IOException {
        SavedItemStore store = loaded();
        store.put(item("Good", "{a}"));

        assertThrows(IOException.class, () -> store.put(item("Bad", "{name:\"\ud800\"}")));
        assertNull(store.get("bad"));
        assertNotNull(loaded().get("good"));
        assertNull(loaded().get("bad"));
    }

    @Test
    void handEditedEntriesAreNormalisedOrDropped() throws IOException {
        String json = "{"
                + "\"X\":{\"name\":\"Starter\",\"item\":\"{a}\"},"
                + "\"empty\":{\"name\":\"Empty\",\"item\":\"\"},"
                + "\"bad\":{\"name\":\"has space\",\"item\":\"{b}\"}"
                + "}";
        Files.write(dir.resolve("saved_items.json"), json.getBytes(StandardCharsets.UTF_8));
        SavedItemStore store = loaded();

        assertNotNull(store.get("starter"));
        assertNull(store.get("x"));
        assertNull(store.get("empty"));
        assertEquals(1, store.list().size());
    }
}
