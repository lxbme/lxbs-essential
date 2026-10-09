package com.lxb.lxbsessential.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class NamesTest {

    private static String repeat(String s, int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    @Test
    void acceptsSimpleName() {
        assertNull(Names.validate("base"));
    }

    @Test
    void acceptsChinese() {
        assertNull(Names.validate("主基地"));
    }

    @Test
    void accepts32CodePoints() {
        assertNull(Names.validate(repeat("a", 32)));
        assertNull(Names.validate(repeat("家", 32)));
    }

    @Test
    void rejectsNullAndEmpty() {
        assertNotNull(Names.validate(null));
        assertNotNull(Names.validate(""));
    }

    @Test
    void rejects33CodePoints() {
        assertNotNull(Names.validate(repeat("a", 33)));
    }

    @Test
    void rejectsWhitespace() {
        assertNotNull(Names.validate("a b"));
        assertNotNull(Names.validate("a\tb"));
    }

    @Test
    void rejectsFormattingCode() {
        assertNotNull(Names.validate("a§cb"));
    }

    @Test
    void keyIsCaseInsensitive() {
        assertEquals("base", Names.key("BaSe"));
        assertEquals(Names.key("Base"), Names.key("BASE"));
    }

    @Test
    void describeShowsDimensionAndBlockCoordinates() {
        Location loc = new Location(-1, 10.7, 64.0, -2.3, 0F, 0F);
        assertEquals("dim -1 (10, 64, -3)", loc.describe());
    }
}
