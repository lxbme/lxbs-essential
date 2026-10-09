package com.lxb.lxbsessential.bed;

import java.util.Locale;

/** Matches block registry names against patterns like {@code modid:name} or {@code modid:*}. */
public final class BlockPatterns {

    private BlockPatterns() {
    }

    public static boolean matchesAny(String[] patterns, String id) {
        for (String raw : patterns) {
            if (raw == null) {
                continue;
            }
            String pattern = raw.trim().toLowerCase(Locale.ROOT);
            if (pattern.isEmpty()) {
                continue;
            }
            if (pattern.endsWith(":*")) {
                if (id.startsWith(pattern.substring(0, pattern.length() - 1))) {
                    return true;
                }
            } else if (pattern.equals(id)) {
                return true;
            }
        }
        return false;
    }
}
