package com.lxb.lxbsessential.phase.fake;

import net.minecraft.world.World;

/** Mirrors the shape of SRP's SRPSaveData so SrpBridge can be tested without SRP installed. */
public class FakeSaveData {

    public static FakeSaveData dimension = new FakeSaveData(0, 0);

    public byte phase;
    public int points;

    public FakeSaveData(int phase, int points) {
        this.phase = (byte) phase;
        this.points = points;
    }

    public static FakeSaveData get(World world) {
        return dimension;
    }

    public byte getEvolutionPhase(int dim) {
        return phase;
    }

    public int getTotalKills(int dim) {
        return points;
    }
}
