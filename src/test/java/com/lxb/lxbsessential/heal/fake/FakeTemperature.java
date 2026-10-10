package com.lxb.lxbsessential.heal.fake;

/** Mirrors SimpleDifficulty's ITemperatureCapability setters. */
public final class FakeTemperature {

    public int level = 24;
    public int damageCounter = 6;

    public void setTemperatureLevel(int level) {
        this.level = level;
    }

    public void setTemperatureDamageCounter(int damageCounter) {
        this.damageCounter = damageCounter;
    }
}
