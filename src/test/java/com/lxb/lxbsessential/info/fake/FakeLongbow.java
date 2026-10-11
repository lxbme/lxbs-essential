package com.lxb.lxbsessential.info.fake;

import net.minecraft.item.ItemBow;

/** Mirrors Spartan Weaponry's ItemLongbow: a bow subclass with its own full-draw speed and draw time. */
public class FakeLongbow extends ItemBow {

    public float getMaxArrowSpeed() {
        return 1.5F;
    }

    public int getDrawTicks() {
        return 30;
    }
}
