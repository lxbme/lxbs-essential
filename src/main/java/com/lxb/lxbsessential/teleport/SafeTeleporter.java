package com.lxb.lxbsessential.teleport;

import com.lxb.lxbsessential.data.Location;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ITeleporter;

/** Places the player at an exact spot after a dimension change, without looking for or building portals. */
public final class SafeTeleporter implements ITeleporter {

    private final Location target;

    public SafeTeleporter(Location target) {
        this.target = target;
    }

    @Override
    public void placeEntity(World world, Entity entity, float yaw) {
        entity.setLocationAndAngles(target.x, target.y, target.z, target.yaw, target.pitch);
        entity.motionX = 0;
        entity.motionY = 0;
        entity.motionZ = 0;
    }
}
