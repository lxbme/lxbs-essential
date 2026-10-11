package com.lxb.lxbsessential.info.fake;

import net.minecraft.item.ItemBow;

/** A longbow whose stats cannot be read, e.g. because the mod's config is not loaded. */
public class FakeBrokenLongbow extends ItemBow {

    public float getMaxArrowSpeed() {
        throw new IllegalStateException("config not loaded");
    }

    public int getDrawTicks() {
        throw new IllegalStateException("config not loaded");
    }
}
