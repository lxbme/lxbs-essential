package com.lxb.lxbsessential.heal.fake;

import net.minecraft.entity.player.EntityPlayer;

/** Mirrors SimpleDifficulty's SDCapabilities lookups. */
public final class FakeSDCapabilities {

    public static FakeThirst thirst = new FakeThirst();
    public static FakeTemperature temperature = new FakeTemperature();

    private FakeSDCapabilities() {
    }

    public static FakeThirst getThirstData(EntityPlayer player) {
        return thirst;
    }

    public static FakeTemperature getTemperatureData(EntityPlayer player) {
        return temperature;
    }
}
