package com.lxb.lxbsessential.teleport;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarmupRulesTest {

    @Test
    void standingStillIsFine() {
        assertFalse(WarmupRules.movedTooFar(10, 20, 10, 20));
    }

    @Test
    void smallJitterIsFine() {
        assertFalse(WarmupRules.movedTooFar(0, 0, 0.3, 0.3));
    }

    @Test
    void exactlyHalfBlockIsFine() {
        assertFalse(WarmupRules.movedTooFar(0, 0, 0.5, 0));
    }

    @Test
    void movingFurtherCancels() {
        assertTrue(WarmupRules.movedTooFar(0, 0, 0.51, 0));
        assertTrue(WarmupRules.movedTooFar(0, 0, 0.4, -0.4));
    }

    @Test
    void secondsLeftRoundsUpAndStopsAtZero() {
        assertEquals(3, WarmupRules.secondsLeft(0, 60));
        assertEquals(3, WarmupRules.secondsLeft(1, 60));
        assertEquals(1, WarmupRules.secondsLeft(41, 60));
        assertEquals(0, WarmupRules.secondsLeft(60, 60));
        assertEquals(0, WarmupRules.secondsLeft(70, 60));
    }
}
