package com.lxb.lxbsessential.phase.fake;

import net.minecraft.world.World;

public final class FakeThrowingSaveData {

    private FakeThrowingSaveData() {
    }

    public static FakeThrowingSaveData get(World world) {
        throw new IllegalStateException("world data not loaded");
    }

    public byte getEvolutionPhase(int dim) {
        return 0;
    }

    public int getTotalKills(int dim) {
        return 0;
    }
}
