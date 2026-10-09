package com.lxb.lxbsessential.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A home or warp: its display name plus where it points. */
public final class NamedLocation {

    public final String name;
    public final Location location;

    public NamedLocation(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    /**
     * Rebuilds a map loaded from disk so every key is {@link Names#key} of its entry's name.
     * Entries that are null, have no location or carry an invalid name are dropped,
     * so hand-edited files never break lookups.
     */
    public static LinkedHashMap<String, NamedLocation> normalize(Map<String, NamedLocation> raw) {
        LinkedHashMap<String, NamedLocation> out = new LinkedHashMap<>();
        if (raw == null) {
            return out;
        }
        for (NamedLocation entry : raw.values()) {
            if (entry == null || entry.location == null || Names.validate(entry.name) != null) {
                continue;
            }
            out.put(Names.key(entry.name), entry);
        }
        return out;
    }

    /** Copy of the values ordered by case-insensitive name. */
    public static List<NamedLocation> sorted(Collection<NamedLocation> values) {
        List<NamedLocation> list = new ArrayList<>(values);
        list.sort(Comparator.comparing(e -> Names.key(e.name)));
        return list;
    }
}
