package com.lxb.lxbsessential.bank;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;

/** Holds the bank of the running server. */
public final class Bank {

    private static final Logger LOGGER = LogManager.getLogger("lxbsessential");

    private static BankService service;

    private Bank() {
    }

    public static void open(Path root) {
        LedgerStore store = new LedgerStore(root);
        service = new BankService(store.load(), store);
    }

    /** Saves once more so online-time progress since the last interest payment is kept. */
    public static void close() {
        if (service != null) {
            saveQuietly();
        }
        service = null;
    }

    public static boolean isOpen() {
        return service != null;
    }

    public static BankService service() {
        return service;
    }

    public static void saveQuietly() {
        try {
            service.save();
        } catch (IOException e) {
            LOGGER.error("Failed to save bank.json", e);
        }
    }
}
