package com.lxb.lxbsessential.bank;

/**
 * Withdrawal fee. Rounded up so tiny withdrawals cannot dodge it; it keeps XP Tomes the better
 * choice for short-term carrying, while the bank is for long-term savings.
 */
public final class Fees {

    private Fees() {
    }

    public static long fee(long taken, double feePercent) {
        if (taken <= 0 || feePercent <= 0) {
            return 0;
        }
        return (long) Math.ceil(taken * feePercent / 100.0);
    }

    public static long received(long taken, double feePercent) {
        return taken - fee(taken, feePercent);
    }

    /** Smallest amount to take from the balance so that the player receives at least {@code needed}. */
    public static long costFor(long needed, double feePercent) {
        if (needed <= 0) {
            return 0;
        }
        if (feePercent <= 0) {
            return needed;
        }
        long cost = (long) Math.ceil(needed / (1 - feePercent / 100.0));
        while (received(cost, feePercent) < needed) {
            cost++;
        }
        while (cost > needed && received(cost - 1, feePercent) >= needed) {
            cost--;
        }
        return cost;
    }
}
