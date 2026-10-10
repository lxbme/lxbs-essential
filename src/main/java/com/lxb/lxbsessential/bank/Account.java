package com.lxb.lxbsessential.bank;

/** One player's bank account as stored in bank.json. */
public final class Account {

    /** Name at the player's last login, for display and lookups by name. */
    String name = "";
    long balance;
    /** Online ticks counted towards the next interest period. */
    long onlineTicks;
    /** Fraction of a point carried over between interest periods. */
    double carry;
    /** Transfers received while offline, announced once at the next login. */
    long pendingReceived;

    public Account() {
    }

    Account copy() {
        Account copy = new Account();
        copy.name = name;
        copy.balance = balance;
        copy.onlineTicks = onlineTicks;
        copy.carry = carry;
        copy.pendingReceived = pendingReceived;
        return copy;
    }
}
