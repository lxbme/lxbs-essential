package com.lxb.lxbsessential.bank;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** All bank accounts, keyed by player UUID. Every balance stays within 0..Long.MAX_VALUE. */
public final class Ledger {

    public enum PayResult { OK, INVALID_AMOUNT, SELF, INSUFFICIENT }

    private final LinkedHashMap<String, Account> accounts;

    public Ledger() {
        this(new LinkedHashMap<String, Account>());
    }

    private Ledger(LinkedHashMap<String, Account> accounts) {
        this.accounts = accounts;
    }

    /** Builds a ledger from bank.json, dropping or repairing anything a hand edit may have broken. */
    public static Ledger fromStorage(Map<String, Account> raw) {
        LinkedHashMap<String, Account> clean = new LinkedHashMap<>();
        if (raw != null) {
            for (Map.Entry<String, Account> entry : raw.entrySet()) {
                Account account = entry.getValue();
                UUID id = parseUuid(entry.getKey());
                if (account == null || id == null) {
                    continue;
                }
                if (account.name == null) {
                    account.name = "";
                }
                account.balance = Math.max(0, account.balance);
                account.onlineTicks = Math.max(0, account.onlineTicks);
                account.pendingReceived = Math.max(0, account.pendingReceived);
                if (Double.isNaN(account.carry) || account.carry < 0 || account.carry >= 1) {
                    account.carry = 0;
                }
                clean.put(id.toString(), account);
            }
        }
        return new Ledger(clean);
    }

    public Map<String, Account> toStorage() {
        return accounts;
    }

    public Ledger copy() {
        LinkedHashMap<String, Account> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Account> entry : accounts.entrySet()) {
            copy.put(entry.getKey(), entry.getValue().copy());
        }
        return new Ledger(copy);
    }

    public void restore(Ledger snapshot) {
        accounts.clear();
        accounts.putAll(snapshot.copy().accounts);
    }

    /** Creates the account if needed and records the player's current name. */
    public void touch(UUID id, String name) {
        account(id, name);
    }

    public String name(UUID id) {
        Account account = accounts.get(id.toString());
        return account == null ? "" : account.name;
    }

    public long balance(UUID id) {
        Account account = accounts.get(id.toString());
        return account == null ? 0 : account.balance;
    }

    public void deposit(UUID id, String name, long points) {
        Account account = account(id, name);
        account.balance = addCapped(account.balance, Math.max(0, points));
    }

    /** Removes up to {@code points} and returns how many were actually removed. */
    public long take(UUID id, long points) {
        Account account = accounts.get(id.toString());
        if (account == null || points <= 0) {
            return 0;
        }
        long taken = Math.min(points, account.balance);
        account.balance -= taken;
        return taken;
    }

    public PayResult pay(UUID from, UUID to, String toName, long points, boolean recipientOnline) {
        if (points <= 0) {
            return PayResult.INVALID_AMOUNT;
        }
        if (from.equals(to)) {
            return PayResult.SELF;
        }
        if (balance(from) < points) {
            return PayResult.INSUFFICIENT;
        }
        take(from, points);
        Account recipient = account(to, toName);
        recipient.balance = addCapped(recipient.balance, points);
        if (!recipientOnline) {
            recipient.pendingReceived = addCapped(recipient.pendingReceived, points);
        }
        return PayResult.OK;
    }

    public long setBalance(UUID id, String name, long points) {
        Account account = account(id, name);
        account.balance = Math.max(0, points);
        return account.balance;
    }

    public long addBalance(UUID id, String name, long delta) {
        Account account = account(id, name);
        account.balance = addCapped(account.balance, delta);
        return account.balance;
    }

    /** Counts one online tick; when a full period has passed, credits interest and returns the amount. */
    public long tickOnline(UUID id, String name, long periodTicks, double ratePercent, long capPoints) {
        Account account = account(id, name);
        account.onlineTicks++;
        if (account.onlineTicks < periodTicks) {
            return 0;
        }
        account.onlineTicks -= periodTicks;
        Interest.Result result = Interest.accrue(account.balance, account.carry, ratePercent, capPoints);
        account.balance = addCapped(account.balance, result.credited);
        account.carry = result.carry;
        return result.credited;
    }

    public long ticksUntilInterest(UUID id, long periodTicks) {
        Account account = accounts.get(id.toString());
        long counted = account == null ? 0 : account.onlineTicks;
        return Math.max(0, periodTicks - counted);
    }

    public UUID findByName(String name) {
        for (Map.Entry<String, Account> entry : accounts.entrySet()) {
            if (entry.getValue().name.equalsIgnoreCase(name)) {
                return UUID.fromString(entry.getKey());
            }
        }
        return null;
    }

    public long takePendingReceived(UUID id) {
        Account account = accounts.get(id.toString());
        if (account == null) {
            return 0;
        }
        long pending = account.pendingReceived;
        account.pendingReceived = 0;
        return pending;
    }

    /** a + b, kept within 0..Long.MAX_VALUE. */
    public static long addCapped(long a, long b) {
        if (b > 0 && a > Long.MAX_VALUE - b) {
            return Long.MAX_VALUE;
        }
        return Math.max(0, a + b);
    }

    private Account account(UUID id, String name) {
        Account account = accounts.get(id.toString());
        if (account == null) {
            account = new Account();
            accounts.put(id.toString(), account);
        }
        if (name != null && !name.isEmpty()) {
            account.name = name;
        }
        return account;
    }

    private static UUID parseUuid(String key) {
        try {
            return key == null ? null : UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
