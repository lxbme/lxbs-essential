package com.lxb.lxbsessential.teleport;

import java.util.function.IntPredicate;

/** Checks run when a teleport is requested, before any warm-up starts. */
public final class TeleportChecks {

    public enum Result { OK, BLOCKED_HERE, BLOCKED_TARGET, MISSING_TARGET }

    private TeleportChecks() {
    }

    public static Result check(int fromDim, int toDim, IntPredicate isBlocked, IntPredicate isRegistered) {
        if (isBlocked.test(fromDim)) {
            return Result.BLOCKED_HERE;
        }
        if (isBlocked.test(toDim)) {
            return Result.BLOCKED_TARGET;
        }
        if (!isRegistered.test(toDim)) {
            return Result.MISSING_TARGET;
        }
        return Result.OK;
    }
}
