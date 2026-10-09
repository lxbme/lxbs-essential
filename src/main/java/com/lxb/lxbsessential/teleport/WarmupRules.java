package com.lxb.lxbsessential.teleport;

/** Pure rules for the teleport warm-up so they can be tested without Minecraft. */
public final class WarmupRules {

    public static final double MAX_HORIZONTAL_MOVE = 0.5;

    private WarmupRules() {
    }

    /** Only horizontal distance counts, so looking around, jumping or falling never cancels. */
    public static boolean movedTooFar(double startX, double startZ, double x, double z) {
        double dx = x - startX;
        double dz = z - startZ;
        return dx * dx + dz * dz > MAX_HORIZONTAL_MOVE * MAX_HORIZONTAL_MOVE;
    }

    public static int secondsLeft(long nowTick, long executeAtTick) {
        long ticks = Math.max(0, executeAtTick - nowTick);
        return (int) ((ticks + 19) / 20);
    }
}
