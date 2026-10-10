package com.lxb.lxbsessential.bank;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankServiceTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");

    @Test
    void commitSavesTheChangedLedger() throws IOException {
        List<Long> savedBalances = new ArrayList<>();
        BankService service = new BankService(new Ledger(), ledger -> savedBalances.add(ledger.balance(A)));

        long result = service.commit(ledger -> {
            ledger.deposit(A, "Alice", 50);
            return ledger.balance(A);
        });

        assertEquals(50, result);
        assertEquals(1, savedBalances.size());
        assertEquals(50L, (long) savedBalances.get(0));
    }

    /** If bank.json cannot be written, nothing may change: the caller then leaves the player's XP alone. */
    @Test
    void failedSaveRestoresLedger() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 100);
        BankService service = new BankService(ledger, l -> {
            throw new IOException("disk full");
        });

        assertThrows(IOException.class, () -> service.commit(l -> {
            l.deposit(A, "Alice", 50);
            return null;
        }));
        assertEquals(100, service.ledger().balance(A));
    }
}
