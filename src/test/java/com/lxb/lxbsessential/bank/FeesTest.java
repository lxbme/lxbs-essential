package com.lxb.lxbsessential.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FeesTest {

    @Test
    void feeRoundsUp() {
        assertEquals(5, Fees.fee(100, 5.0));
        assertEquals(6, Fees.fee(101, 5.0));
        assertEquals(1, Fees.fee(1, 5.0));
        assertEquals(0, Fees.fee(0, 5.0));
        assertEquals(0, Fees.fee(100, 0.0));
    }

    @Test
    void receivedIsTakenMinusFee() {
        assertEquals(95, Fees.received(100, 5.0));
        assertEquals(100, Fees.received(100, 0.0));
    }

    /** The bank must neither overcharge nor shortchange: cost is the smallest sufficient amount. */
    @Test
    void costForIsTheSmallestSufficientAmount() {
        for (long needed = 1; needed <= 5000; needed++) {
            long cost = Fees.costFor(needed, 5.0);
            assertTrue(Fees.received(cost, 5.0) >= needed, "needed " + needed);
            assertTrue(Fees.received(cost - 1, 5.0) < needed, "needed " + needed);
        }
    }

    @Test
    void costForKnownValues() {
        assertEquals(100, Fees.costFor(95, 5.0));
        assertEquals(0, Fees.costFor(0, 5.0));
    }

    @Test
    void freeWithdrawalsCostExactlyWhatIsNeeded() {
        assertEquals(1234, Fees.costFor(1234, 0.0));
    }

    /** A high fee with a huge withdrawal must not loop forever on the server thread. */
    @Test
    void costForTerminatesForHugeAmountsAtHighFees() throws InterruptedException {
        long[] result = new long[1];
        Thread worker = new Thread(() -> result[0] = Fees.costFor(4_499_999_837_500_002_220L, 99.0));
        worker.setDaemon(true);
        worker.start();
        worker.join(2000);
        assertTrue(!worker.isAlive(), "costFor did not finish");
        assertEquals(Long.MAX_VALUE, result[0]);
    }
}
