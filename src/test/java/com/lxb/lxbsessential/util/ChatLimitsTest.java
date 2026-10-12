package com.lxb.lxbsessential.util;

import net.minecraft.util.text.TextComponentString;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChatLimitsTest {

    private static String repeat(char c, int times) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < times; i++) {
            text.append(c);
        }
        return text.toString();
    }

    @Test
    void shortLineFits() {
        assertTrue(ChatLimits.fits(new TextComponentString("hello")));
    }

    /** The packet limit is in UTF-8 bytes: 11000 Chinese characters are 33000 bytes, 11000 ASCII ones are not. */
    @Test
    void limitCountsUtf8Bytes() {
        assertFalse(ChatLimits.fits(new TextComponentString(repeat('中', 11000))));
        assertTrue(ChatLimits.fits(new TextComponentString(repeat('x', 11000))));
    }
}
