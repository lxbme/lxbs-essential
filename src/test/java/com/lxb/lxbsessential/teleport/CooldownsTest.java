package com.lxb.lxbsessential.teleport;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CooldownsTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");

    @Test
    void noCooldownByDefault() {
        assertEquals(0, new Cooldowns().remainingMillis(A, 1000));
    }

    @Test
    void remainingCountsDownAndExpires() {
        Cooldowns cooldowns = new Cooldowns();
        cooldowns.start(A, 1000, 10);
        assertEquals(10000, cooldowns.remainingMillis(A, 1000));
        assertEquals(1, cooldowns.remainingMillis(A, 10999));
        assertEquals(0, cooldowns.remainingMillis(A, 11000));
        assertEquals(0, cooldowns.remainingMillis(A, 50000));
    }

    @Test
    void zeroSecondsRecordsNothing() {
        Cooldowns cooldowns = new Cooldowns();
        cooldowns.start(A, 1000, 0);
        assertEquals(0, cooldowns.remainingMillis(A, 1000));
    }

    @Test
    void playersAreIndependent() {
        Cooldowns cooldowns = new Cooldowns();
        cooldowns.start(A, 1000, 10);
        assertEquals(0, cooldowns.remainingMillis(B, 1000));
    }

    @Test
    void clearRemovesAll() {
        Cooldowns cooldowns = new Cooldowns();
        cooldowns.start(A, 1000, 10);
        cooldowns.clear();
        assertEquals(0, cooldowns.remainingMillis(A, 1000));
    }

    @Test
    void displaySecondsRoundUp() {
        assertEquals(0, Cooldowns.toDisplaySeconds(0));
        assertEquals(1, Cooldowns.toDisplaySeconds(1));
        assertEquals(1, Cooldowns.toDisplaySeconds(1000));
        assertEquals(2, Cooldowns.toDisplaySeconds(1001));
    }
}
