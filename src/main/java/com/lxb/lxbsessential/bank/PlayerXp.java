package com.lxb.lxbsessential.bank;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

/** Reads and writes a player's experience; never trusts vanilla's experienceTotal, which enchanting does not lower. */
public final class PlayerXp {

    private PlayerXp() {
    }

    public static long total(EntityPlayer player) {
        return XpMath.total(player.experienceLevel, player.experience);
    }

    public static void set(EntityPlayerMP player, int level, float progress) {
        player.experienceLevel = level;
        player.experience = progress;
        player.experienceTotal = (int) Math.min(Integer.MAX_VALUE, XpMath.total(level, progress));
        // addExperienceLevel(0) resets the server's cached value so the new XP is sent to the client next tick
        player.addExperienceLevel(0);
    }
}
