package com.lxb.lxbsessential.fix;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public final class Repair {

    private Repair() {
    }

    /** Restores full durability through Forge's item hooks, leaving NBT untouched. Returns false if nothing changed. */
    public static boolean repair(ItemStack stack) {
        if (!stack.isItemStackDamageable() || !stack.isItemDamaged()) {
            return false;
        }
        stack.setItemDamage(0);
        return true;
    }

    /** Returns how many items were repaired. */
    public static int repair(EntityPlayer player, FixTarget target) {
        int repaired = 0;
        for (EntityEquipmentSlot slot : target.slots()) {
            if (repair(player.getItemStackFromSlot(slot))) {
                repaired++;
            }
        }
        return repaired;
    }
}
