package com.lxb.lxbsessential.bank;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LedgerStoreTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");

    @TempDir
    Path dir;

    @Test
    void saveThenLoadRoundTrips() throws IOException {
        LedgerStore store = new LedgerStore(dir);
        Ledger ledger = store.load();
        ledger.deposit(A, "Alice", 1234);
        ledger.tickOnline(A, "Alice", 100, 1.0, 30970);
        store.save(ledger);

        Ledger loaded = new LedgerStore(dir).load();
        assertEquals(1234, loaded.balance(A));
        assertEquals("Alice", loaded.name(A));
        assertEquals(99, loaded.ticksUntilInterest(A, 100));
    }

    @Test
    void missingFileGivesEmptyLedger() {
        assertEquals(0, new LedgerStore(dir).load().toStorage().size());
    }

    @Test
    void corruptFileIsQuarantined() throws IOException {
        Files.write(dir.resolve("bank.json"), "{oops".getBytes(StandardCharsets.UTF_8));
        assertEquals(0, new LedgerStore(dir).load().toStorage().size());
        try (Stream<Path> files = Files.list(dir)) {
            assertTrue(files.anyMatch(p -> p.getFileName().toString().startsWith("bank.json.corrupt-")));
        }
    }

    @Test
    void handEditedFileLoads() throws IOException {
        String json = "{\"" + A + "\": {\"balance\": 7}, \"junk\": {\"balance\": 3}}";
        Files.write(dir.resolve("bank.json"), json.getBytes(StandardCharsets.UTF_8));
        Ledger ledger = new LedgerStore(dir).load();
        assertEquals(7, ledger.balance(A));
        assertEquals("", ledger.name(A));
        assertEquals(1, ledger.toStorage().size());
    }
}
