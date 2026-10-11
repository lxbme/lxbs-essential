package com.lxb.lxbsessential.bank;

import java.io.IOException;
import java.util.UUID;
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

    /** Rejected transfers change nothing, so they are answered without writing bank.json. */
    public Ledger.PayResult pay(UUID from, UUID to, String toName, long points, boolean recipientOnline)
            throws IOException {
        Ledger.PayResult check = ledger.checkPay(from, to, points);
        if (check != Ledger.PayResult.OK) {
            return check;
        }
        return commit(l -> l.pay(from, to, toName, points, recipientOnline));
    }

    public void save() throws IOException {
        saver.save(ledger);
    }
}
