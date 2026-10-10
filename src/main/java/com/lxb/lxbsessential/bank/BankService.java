package com.lxb.lxbsessential.bank;

import java.io.IOException;
import java.util.function.Function;

/** Applies a ledger change and saves it; if saving fails the change is rolled back in memory. */
public final class BankService {

    private final Ledger ledger;
    private final LedgerSaver saver;

    public BankService(Ledger ledger, LedgerSaver saver) {
        this.ledger = ledger;
        this.saver = saver;
    }

    public Ledger ledger() {
        return ledger;
    }

    public <T> T commit(Function<Ledger, T> change) throws IOException {
        Ledger snapshot = ledger.copy();
        T result = change.apply(ledger);
        try {
            saver.save(ledger);
        } catch (IOException e) {
            ledger.restore(snapshot);
            throw e;
        }
        return result;
    }

    public void save() throws IOException {
        saver.save(ledger);
    }
}
