package com.lxb.lxbsessential.bank;

/** One interest period: compound interest on the part of the balance below the cap. */
public final class Interest {

    private Interest() {
    }

    public static final class Result {
        public final long credited;
        /** Fraction of a point carried over to the next period. */
        public final double carry;

        Result(long credited, double carry) {
            this.credited = credited;
            this.carry = carry;
        }
    }

    public static Result accrue(long balance, double carry, double ratePercent, long capPoints) {
        long base = Math.max(0, Math.min(balance, capPoints));
        double interest = base * ratePercent / 100.0 + carry;
        long credited = (long) Math.floor(interest);
        return new Result(credited, interest - credited);
    }
}
