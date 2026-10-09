package com.lxb.lxbsessential.data;

import java.util.Locale;

/** Validation and lookup-key rules shared by home and warp names. */
public final class Names {

    public static final int MAX_LENGTH = 32;
    public static final String DEFAULT_HOME = "home";

    private Names() {
    }

    /** Returns an error message for the player, or {@code null} when the name is valid. */
    public static String validate(String name) {
        if (name == null || name.isEmpty() || name.codePointCount(0, name.length()) > MAX_LENGTH) {
            return "Name must be 1-" + MAX_LENGTH + " characters long.";
        }
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isWhitespace(c) || c == '§') {
                return "Name must not contain spaces or formatting codes.";
            }
        }
        return null;
    }

    /** Case-insensitive lookup key; the original spelling is kept for display. */
    public static String key(String name) {
        return name.toLowerCase(Locale.ROOT);
    }
}
