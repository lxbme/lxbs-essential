package com.lxb.lxbsessential.bank;

import com.google.gson.reflect.TypeToken;
import com.lxb.lxbsessential.data.JsonIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/** {@code <root>/bank.json}, written atomically; a corrupt file is moved aside and an empty ledger is used. */
public final class LedgerStore implements LedgerSaver {

    private static final Logger LOGGER = LogManager.getLogger("lxbsessential");
    private static final Type TYPE = new TypeToken<LinkedHashMap<String, Account>>() { }.getType();

    private final Path file;

    public LedgerStore(Path root) {
        this.file = root.resolve("bank.json");
    }

    public Ledger load() {
        JsonIO.Loaded<LinkedHashMap<String, Account>> loaded =
                JsonIO.read(file, TYPE, LinkedHashMap::new, LocalDateTime.now());
        if (loaded.error != null) {
            LOGGER.error("Could not read {}, moved it to {} and started an empty bank", file, loaded.quarantined,
                    loaded.error);
        }
        return Ledger.fromStorage(loaded.value);
    }

    @Override
    public void save(Ledger ledger) throws IOException {
        JsonIO.write(file, ledger.toStorage());
    }
}
