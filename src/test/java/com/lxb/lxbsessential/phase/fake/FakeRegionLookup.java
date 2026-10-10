package com.lxb.lxbsessential.phase.fake;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Mirrors SRPMixins' SRPSaveDataInterface.get, which returns region data when chunk phases apply. */
public final class FakeRegionLookup {

    public static FakeSaveData result;

    private FakeRegionLookup() {
    }

    public static FakeSaveData get(World world, EntityPlayer player, BlockPos pos) {
        return result;
    }
}
