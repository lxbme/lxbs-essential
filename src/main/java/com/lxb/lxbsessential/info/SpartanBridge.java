package com.lxb.lxbsessential.info;

import net.minecraft.item.Item;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/** Optional Spartan Weaponry integration: reads a longbow's full-draw arrow speed and draw time. */
public final class SpartanBridge {

    public static final String LONGBOW = "com.oblivioussp.spartanweaponry.item.ItemLongbow";

    private final Class<?> longbow;
    private final Method maxArrowSpeed;
    private final Method drawTicks;

    private SpartanBridge(Class<?> longbow, Method maxArrowSpeed, Method drawTicks) {
        this.longbow = longbow;
        this.maxArrowSpeed = maxArrowSpeed;
        this.drawTicks = drawTicks;
    }

    /** Returns null when Spartan Weaponry is missing or its longbow API does not match. */
    @Nullable
    public static SpartanBridge bind(String longbowClass) {
        try {
            Class<?> longbow = Class.forName(longbowClass, false, SpartanBridge.class.getClassLoader());
            Method speed = longbow.getMethod("getMaxArrowSpeed");
            Method draw = longbow.getMethod("getDrawTicks");
            if (speed.getReturnType() != float.class || draw.getReturnType() != int.class) {
                return null;
            }
            return new SpartanBridge(longbow, speed, draw);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    public boolean isLongbow(Item item) {
        return longbow.isInstance(item);
    }

    /** Multiplier of the 3.0 launch velocity at full draw. */
    public float maxArrowSpeed(Item item) throws ReflectiveOperationException {
        return (Float) maxArrowSpeed.invoke(item);
    }

    public int drawTicks(Item item) throws ReflectiveOperationException {
        return (Integer) drawTicks.invoke(item);
    }
}
