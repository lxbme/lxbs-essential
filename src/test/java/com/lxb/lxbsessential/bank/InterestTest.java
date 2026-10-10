package com.lxb.lxbsessential.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InterestTest {

    private static final long CAP = 30970;

    @Test
    void onePercentOfTheBalance() {
        Interest.Result result = Interest.accrue(1000, 0, 1.0, CAP);
        assertEquals(10, result.credited);
        assertEquals(0, result.carry, 1e-9);
    }

    @Test
    void interestCompoundsOnTheNewBalance() {
        Interest.Result result = Interest.accrue(1010, 0, 1.0, CAP);
        assertEquals(10, result.credited);
        assertEquals(0.1, result.carry, 1e-9);
    }

    /** Above the cap growth becomes linear: only the capped part earns interest. */
    @Test
    void onlyTheCappedPartEarnsInterest() {
        Interest.Result result = Interest.accrue(100000, 0, 1.0, CAP);
        assertEquals(309, result.credited);
        assertEquals(0.7, result.carry, 1e-9);
    }

    @Test
    void fractionsAccumulateUntilAWholePoint() {
        Interest.Result first = Interest.accrue(50, 0, 1.0, CAP);
        assertEquals(0, first.credited);
        assertEquals(0.5, first.carry, 1e-9);
        Interest.Result second = Interest.accrue(50, first.carry, 1.0, CAP);
        assertEquals(1, second.credited);
        assertEquals(0, second.carry, 1e-9);
    }

    @Test
    void emptyBalanceKeepsItsCarry() {
        Interest.Result result = Interest.accrue(0, 0.4, 1.0, CAP);
        assertEquals(0, result.credited);
        assertEquals(0.4, result.carry, 1e-9);
    }

    @Test
    void zeroRateOrZeroCapPaysNothing() {
        assertEquals(0, Interest.accrue(1000, 0, 0.0, CAP).credited);
        assertEquals(0, Interest.accrue(1000, 0, 1.0, 0).credited);
    }
}
