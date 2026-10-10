package com.lxb.lxbsessential.phase.fake;

import net.minecraft.world.World;

/** Like a future SRP version whose API changed: getTotalKills is gone. */
public final class FakeBrokenSaveData {

    private FakeBrokenSaveData() {
    }

    public static FakeBrokenSaveData get(World world) {
        return new FakeBrokenSaveData();
    }

    public byte getEvolutionPhase(int dim) {
        return 0;
    }
}
