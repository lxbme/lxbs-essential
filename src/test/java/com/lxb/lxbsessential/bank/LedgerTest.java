package com.lxb.lxbsessential.bank;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LedgerTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final long CAP = 30970;

    @Test
    void depositAddsToBalanceAndRemembersName() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 500);
        assertEquals(500, ledger.balance(A));
        assertEquals("Alice", ledger.name(A));
        assertEquals(0, ledger.balance(B));
    }

    @Test
    void takeNeverExceedsBalance() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 100);
        assertEquals(30, ledger.take(A, 30));
        assertEquals(70, ledger.balance(A));
        assertEquals(70, ledger.take(A, 500));
        assertEquals(0, ledger.balance(A));
        assertEquals(0, ledger.take(B, 10));
    }

    @Test
    void payMovesPointsBetweenAccounts() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 100);
        assertEquals(Ledger.PayResult.OK, ledger.pay(A, B, "Bob", 40, true));
        assertEquals(60, ledger.balance(A));
        assertEquals(40, ledger.balance(B));
        assertEquals("Bob", ledger.name(B));
        assertEquals(0, ledger.takePendingReceived(B));
    }

    @Test
    void payToOfflinePlayerIsAnnouncedOnceOnLogin() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 100);
        ledger.pay(A, B, "Bob", 10, false);
        ledger.pay(A, B, "Bob", 5, false);
        assertEquals(15, ledger.takePendingReceived(B));
        assertEquals(0, ledger.takePendingReceived(B));
    }

    @Test
    void payRejectsBadRequestsWithoutChangingBalances() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 100);
        assertEquals(Ledger.PayResult.INVALID_AMOUNT, ledger.pay(A, B, "Bob", 0, true));
        assertEquals(Ledger.PayResult.INVALID_AMOUNT, ledger.pay(A, B, "Bob", -5, true));
        assertEquals(Ledger.PayResult.SELF, ledger.pay(A, A, "Alice", 10, true));
        assertEquals(Ledger.PayResult.INSUFFICIENT, ledger.pay(A, B, "Bob", 101, true));
        assertEquals(100, ledger.balance(A));
        assertEquals(0, ledger.balance(B));
    }

    @Test
    void adminChangesNeverGoBelowZero() {
        Ledger ledger = new Ledger();
        assertEquals(0, ledger.setBalance(A, "Alice", -5));
        assertEquals(50, ledger.addBalance(A, "Alice", 50));
        assertEquals(0, ledger.addBalance(A, "Alice", -80));
    }

    @Test
    void balancesNeverOverflow() {
        Ledger ledger = new Ledger();
        ledger.setBalance(A, "Alice", Long.MAX_VALUE - 1);
        assertEquals(Long.MAX_VALUE, ledger.addBalance(A, "Alice", 10));
        ledger.deposit(A, "Alice", 10);
        assertEquals(Long.MAX_VALUE, ledger.balance(A));
        assertEquals(Long.MAX_VALUE, Ledger.addCapped(Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(0, Ledger.addCapped(5, -10));
    }

    @Test
    void findByNameIsCaseInsensitiveAndFollowsRenames() {
        Ledger ledger = new Ledger();
        ledger.touch(A, "OldName");
        ledger.touch(A, "Alice");
        assertEquals(A, ledger.findByName("ALICE"));
        assertNull(ledger.findByName("oldname"));
        assertNull(ledger.findByName("nobody"));
    }

    @Test
    void interestIsCreditedAfterAFullPeriodOnline() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 1000);
        for (int tick = 1; tick < 10; tick++) {
            assertEquals(0, ledger.tickOnline(A, "Alice", 10, 1.0, CAP));
        }
        assertEquals(10, ledger.tickOnline(A, "Alice", 10, 1.0, CAP));
        assertEquals(1010, ledger.balance(A));
        assertEquals(10, ledger.ticksUntilInterest(A, 10));
    }

    @Test
    void ticksUntilInterestCountsDown() {
        Ledger ledger = new Ledger();
        for (int tick = 0; tick < 3; tick++) {
            ledger.tickOnline(A, "Alice", 10, 1.0, CAP);
        }
        assertEquals(7, ledger.ticksUntilInterest(A, 10));
        assertEquals(10, ledger.ticksUntilInterest(B, 10));
    }

    @Test
    void restoreUndoesChangesSinceTheSnapshot() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 100);
        Ledger snapshot = ledger.copy();
        ledger.deposit(A, "Alice", 50);
        ledger.deposit(B, "Bob", 7);
        ledger.restore(snapshot);
        assertEquals(100, ledger.balance(A));
        assertEquals(0, ledger.balance(B));
        assertNull(ledger.findByName("Bob"));
    }

    @Test
    void fromStorageRepairsHandEditedData() {
        Account broken = new Account();
        broken.name = null;
        broken.balance = -5;
        broken.onlineTicks = -3;
        broken.carry = Double.NaN;
        broken.pendingReceived = -1;
        Map<String, Account> raw = new LinkedHashMap<>();
        raw.put("not-a-uuid", new Account());
        raw.put(A.toString(), null);
        raw.put(B.toString(), broken);

        Ledger ledger = Ledger.fromStorage(raw);
        assertEquals(1, ledger.toStorage().size());
        assertEquals(0, ledger.balance(B));
        assertEquals("", ledger.name(B));
        assertEquals(10, ledger.ticksUntilInterest(B, 10));
        assertEquals(0, ledger.takePendingReceived(B));
        assertEquals(0, ledger.tickOnline(B, "Bob", 1, 1.0, CAP));
    }

    @Test
    void fromStorageOfNothingIsEmpty() {
        assertEquals(0, Ledger.fromStorage(null).toStorage().size());
    }

    /** UUID keys are case-insensitive; two hand-edited spellings of one key must merge, not overwrite. */
    @Test
    void fromStorageMergesKeysThatDifferOnlyInCase() {
        Account lower = new Account();
        lower.name = "Alice";
        lower.balance = 10;
        lower.onlineTicks = 3;
        lower.pendingReceived = 2;
        Account upper = new Account();
        upper.name = "";
        upper.balance = 5;
        upper.onlineTicks = 7;
        upper.pendingReceived = 4;
        Map<String, Account> raw = new LinkedHashMap<>();
        raw.put("0000000a-0000-0000-0000-00000000abcd", lower);
        raw.put("0000000A-0000-0000-0000-00000000ABCD", upper);

        Ledger ledger = Ledger.fromStorage(raw);
        UUID id = UUID.fromString("0000000a-0000-0000-0000-00000000abcd");
        assertEquals(1, ledger.toStorage().size());
        assertEquals(15, ledger.balance(id));
        assertEquals("Alice", ledger.name(id));
        assertEquals(3, ledger.ticksUntilInterest(id, 10));
        assertEquals(6, ledger.takePendingReceived(id));
    }

    @Test
    void checkPayReportsWithoutChangingAnything() {
        Ledger ledger = new Ledger();
        ledger.deposit(A, "Alice", 100);
        assertEquals(Ledger.PayResult.OK, ledger.checkPay(A, B, 40));
        assertEquals(Ledger.PayResult.INVALID_AMOUNT, ledger.checkPay(A, B, 0));
        assertEquals(Ledger.PayResult.SELF, ledger.checkPay(A, A, 10));
        assertEquals(Ledger.PayResult.INSUFFICIENT, ledger.checkPay(A, B, 101));
        assertEquals(100, ledger.balance(A));
        assertEquals(0, ledger.balance(B));
    }
}
