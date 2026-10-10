package com.lxb.lxbsessential.bank;

import java.io.IOException;

/** Persists the ledger; injected so tests can simulate a failing disk. */
public interface LedgerSaver {

    void save(Ledger ledger) throws IOException;
}
