package com.lxb.lxbsessential;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SmokeTest {

    @Test
    void tagsCarryModId() {
        assertEquals("lxbsessential", Tags.MOD_ID);
        assertEquals("LXB's Essential", Tags.MOD_NAME);
    }
}
