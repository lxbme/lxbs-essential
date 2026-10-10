package com.lxb.lxbsessential.bank;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpMathTest {

    @Test
    void closedFormMatchesLevelByLevelSum() {
        long sum = 0;
        for (int level = 0; level <= 200; level++) {
            assertEquals(sum, XpMath.pointsForLevel(level), "level " + level);
            sum += XpMath.cap(level);
        }
    }

    @Test
    void knownLevelBoundaries() {
        assertEquals(315, XpMath.pointsForLevel(15));
        assertEquals(352, XpMath.pointsForLevel(16));
        assertEquals(394, XpMath.pointsForLevel(17));
        assertEquals(1395, XpMath.pointsForLevel(30));
        assertEquals(1507, XpMath.pointsForLevel(31));
        assertEquals(1628, XpMath.pointsForLevel(32));
        assertEquals(5345, XpMath.pointsForLevel(50));
        assertEquals(30970, XpMath.pointsForLevel(100));
    }

    @Test
    void capPerLevel() {
        assertEquals(7, XpMath.cap(0));
        assertEquals(35, XpMath.cap(14));
        assertEquals(37, XpMath.cap(15));
        assertEquals(107, XpMath.cap(29));
        assertEquals(112, XpMath.cap(30));
        assertEquals(121, XpMath.cap(31));
    }

    /** Vanilla's experienceTotal is not lowered by enchanting, so totals come from level + bar progress. */
    @Test
    void totalAddsProgressOfCurrentLevel() {
        assertEquals(0, XpMath.total(0, 0F));
        assertEquals(1395 + 56, XpMath.total(30, 0.5F));
    }

    @Test
    void pointsBetweenKeepsTheBarProgress() {
        assertEquals(845, XpMath.pointsBetween(20, 30, 0F));
        // level 20 bar is 62 points, level 30 bar is 112 points
        assertEquals((1395 + 56) - (550 + 31), XpMath.pointsBetween(20, 30, 0.5F));
    }

    @Test
    void fromPointsInvertsWholeLevels() {
        int[] levels = {0, 1, 15, 16, 17, 30, 31, 32, 100};
        for (int level : levels) {
            XpMath.Xp xp = XpMath.fromPoints(XpMath.pointsForLevel(level));
            assertEquals(level, xp.level, "level " + level);
            assertEquals(0F, xp.progress, "level " + level);
        }
    }

    @Test
    void fromPointsKeepsProgress() {
        XpMath.Xp xp = XpMath.fromPoints(1451);
        assertEquals(30, xp.level);
        assertEquals(0.5F, xp.progress, 0.01F);
    }

    @Test
    void fromPointsOfNothing() {
        assertEquals(0, XpMath.fromPoints(0).level);
        assertEquals(0, XpMath.fromPoints(-5).level);
        assertEquals(0F, XpMath.fromPoints(-5).progress);
    }
}
