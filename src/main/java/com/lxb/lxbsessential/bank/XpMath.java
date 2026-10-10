package com.lxb.lxbsessential.bank;

/** Vanilla 1.12 experience curve: converting between levels and experience points. */
public final class XpMath {

    /** Keeps every intermediate value of the closed forms inside a long. */
    public static final int MAX_LEVEL = 1_000_000_000;

    private XpMath() {
    }

    public static final class Xp {
        public final int level;
        public final float progress;

        Xp(int level, float progress) {
            this.level = level;
            this.progress = progress;
        }
    }

    /** Points needed to go from {@code level} to {@code level + 1}. */
    public static long cap(int level) {
        if (level >= 30) {
            return 112L + (level - 30) * 9L;
        }
        if (level >= 15) {
            return 37L + (level - 15) * 5L;
        }
        return 7L + 2L * level;
    }

    /** Points needed to go from level 0 to {@code level}. */
    public static long pointsForLevel(int level) {
        long l = level;
        if (l <= 16) {
            return l * l + 6 * l;
        }
        if (l <= 31) {
            return (5 * l * l - 81 * l + 720) / 2;
        }
        return (9 * l * l - 325 * l + 4440) / 2;
    }

    public static long total(int level, float progress) {
        return pointsForLevel(level) + Math.round(progress * (double) cap(level));
    }

    /** Points between two levels at the same bar progress. */
    public static long pointsBetween(int lowLevel, int highLevel, float progress) {
        return total(highLevel, progress) - total(lowLevel, progress);
    }

    public static Xp fromPoints(long points) {
        if (points <= 0) {
            return new Xp(0, 0F);
        }
        int low = 0;
        int high = MAX_LEVEL;
        while (low < high) {
            int mid = low + (high - low + 1) / 2;
            if (pointsForLevel(mid) <= points) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        long rest = points - pointsForLevel(low);
        return new Xp(low, (float) Math.min(rest / (double) cap(low), 0.99999));
    }
}
