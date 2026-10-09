package com.lxb.lxbsessential.bed;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockPatternsTest {

    @Test
    void exactMatch() {
        assertTrue(BlockPatterns.matchesAny(new String[]{"minecraft:bed"}, "minecraft:bed"));
    }

    @Test
    void modWildcardMatchesEveryBlockOfThatMod() {
        String[] patterns = {"comforts:*"};
        assertTrue(BlockPatterns.matchesAny(patterns, "comforts:sleeping_bag_red"));
        assertTrue(BlockPatterns.matchesAny(patterns, "comforts:hammock_white"));
    }

    @Test
    void modWildcardDoesNotMatchModWithSamePrefix() {
        assertFalse(BlockPatterns.matchesAny(new String[]{"comforts:*"}, "comfortsplus:bed"));
    }

    @Test
    void vanillaBedIsNotBlockedByDefault() {
        assertFalse(BlockPatterns.matchesAny(new String[]{"comforts:*"}, "minecraft:bed"));
    }

    @Test
    void patternCaseAndSurroundingSpacesAreIgnored() {
        assertTrue(BlockPatterns.matchesAny(new String[]{"  Comforts:*  "}, "comforts:sleeping_bag_red"));
    }

    @Test
    void emptyAndNullPatternsAreIgnored() {
        assertFalse(BlockPatterns.matchesAny(new String[]{"", null, "   "}, "minecraft:bed"));
    }
}
