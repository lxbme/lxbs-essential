package com.lxb.lxbsessential.data;

import java.util.Locale;

/** An immutable position in a dimension, including the facing direction. */
public final class Location {

    public final int dim;
    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;

    public Location(int dim, double x, double y, double z, float yaw, float pitch) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    /** Human-readable form such as {@code dim 0 (12, 64, -3)}. */
    public String describe() {
        return String.format(Locale.ROOT, "dim %d (%d, %d, %d)",
                dim, (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    /** Block coordinates as plain numbers, e.g. {@code 12 64 -3}, ready to paste into /tp. */
    public String blockCoords() {
        return String.format(Locale.ROOT, "%d %d %d",
                (int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Location)) {
            return false;
        }
        Location other = (Location) o;
        return dim == other.dim
                && Double.compare(x, other.x) == 0
                && Double.compare(y, other.y) == 0
                && Double.compare(z, other.z) == 0
                && Float.compare(yaw, other.yaw) == 0
                && Float.compare(pitch, other.pitch) == 0;
    }

    @Override
    public int hashCode() {
        int result = dim;
        result = 31 * result + Double.hashCode(x);
        result = 31 * result + Double.hashCode(y);
        result = 31 * result + Double.hashCode(z);
        result = 31 * result + Float.hashCode(yaw);
        result = 31 * result + Float.hashCode(pitch);
        return result;
    }

    @Override
    public String toString() {
        return describe();
    }
}
