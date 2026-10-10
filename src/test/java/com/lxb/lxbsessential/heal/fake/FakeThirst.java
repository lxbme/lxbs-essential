package com.lxb.lxbsessential.heal.fake;

/** Mirrors SimpleDifficulty's IThirstCapability setters. */
public final class FakeThirst {

    public int level = 3;
    public float saturation = 0F;
    public float exhaustion = 2.5F;
    public int damageCounter = 4;

    public void setThirstLevel(int level) {
        this.level = level;
    }

    public void setThirstSaturation(float saturation) {
        this.saturation = saturation;
    }

    public void setThirstExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }

    public void setThirstDamageCounter(int damageCounter) {
        this.damageCounter = damageCounter;
    }
}
