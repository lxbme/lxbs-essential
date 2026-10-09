package com.lxb.lxbsessential.util;

import com.lxb.lxbsessential.data.Location;
import net.minecraft.entity.player.EntityPlayerMP;

public final class Locations {

    private Locations() {
    }

    public static Location of(EntityPlayerMP player) {
        return new Location(player.dimension, player.posX, player.posY, player.posZ,
                player.rotationYaw, player.rotationPitch);
    }
}
