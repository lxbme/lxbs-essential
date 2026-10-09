package com.lxb.lxbsessential.data;

import java.util.LinkedHashMap;
import java.util.List;

/** Everything stored for one player: named homes and the single /back point. */
public final class PlayerData {

    public enum SetResult { CREATED, OVERWRITTEN, LIMIT_REACHED }

    private LinkedHashMap<String, NamedLocation> homes = new LinkedHashMap<>();
    private Location back;

    public PlayerData() {
    }

    public NamedLocation getHome(String name) {
        return homes().get(Names.key(name));
    }

    /** Overwriting an existing name never counts against {@code maxHomes}. */
    public SetResult setHome(String name, Location location, int maxHomes) {
        String key = Names.key(name);
        boolean exists = homes().containsKey(key);
        if (!exists && homes().size() >= maxHomes) {
            return SetResult.LIMIT_REACHED;
        }
        homes().put(key, new NamedLocation(name, location));
        return exists ? SetResult.OVERWRITTEN : SetResult.CREATED;
    }

    public boolean deleteHome(String name) {
        return homes().remove(Names.key(name)) != null;
    }

    public List<NamedLocation> listHomes() {
        return NamedLocation.sorted(homes().values());
    }

    public int homeCount() {
        return homes().size();
    }

    public Location getBack() {
        return back;
    }

    public void setBack(Location back) {
        this.back = back;
    }

    /** Repairs data loaded from a possibly hand-edited file. */
    public void normalize() {
        homes = NamedLocation.normalize(homes);
    }

    private LinkedHashMap<String, NamedLocation> homes() {
        if (homes == null) {
            homes = new LinkedHashMap<>();
        }
        return homes;
    }
}
