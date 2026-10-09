package com.lxb.lxbsessential.teleport;

import org.junit.jupiter.api.Test;

import java.util.function.IntPredicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeleportChecksTest {

    private static final IntPredicate NONE_BLOCKED = dim -> false;
    private static final IntPredicate ALL_REGISTERED = dim -> true;

    @Test
    void okWhenNothingBlockedAndTargetExists() {
        assertEquals(TeleportChecks.Result.OK, TeleportChecks.check(0, -1, NONE_BLOCKED, ALL_REGISTERED));
    }

    @Test
    void blockedCurrentDimensionComesFirst() {
        assertEquals(TeleportChecks.Result.BLOCKED_HERE,
                TeleportChecks.check(7, 7, dim -> dim == 7, dim -> false));
    }

    @Test
    void blockedTargetDimension() {
        assertEquals(TeleportChecks.Result.BLOCKED_TARGET,
                TeleportChecks.check(0, 7, dim -> dim == 7, ALL_REGISTERED));
    }

    /** A removed dimension is reported before the warm-up instead of after it. */
    @Test
    void missingTargetDimension() {
        assertEquals(TeleportChecks.Result.MISSING_TARGET,
                TeleportChecks.check(0, 42, NONE_BLOCKED, dim -> dim != 42));
    }
}
