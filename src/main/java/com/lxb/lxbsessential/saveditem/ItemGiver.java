package com.lxb.lxbsessential.saveditem;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;

import java.util.ArrayList;
import java.util.List;

/** Gives copies of an item the way vanilla /give does. */
public final class ItemGiver {

    /** One player inventory; more than that would only pile up on the ground. */
    public static final int MAX_STACKS = 36;

    private ItemGiver() {
    }

    public static int maxCount(ItemStack template) {
        return MAX_STACKS * template.getMaxStackSize();
    }

    /** Copies of the template totalling count, each no bigger than the item's max stack size. */
    public static List<ItemStack> stacks(ItemStack template, int count) {
        if (count < 1 || count > maxCount(template)) {
            throw new IllegalArgumentException("count " + count + " is outside 1.." + maxCount(template));
        }
        int perStack = template.getMaxStackSize();
        List<ItemStack> stacks = new ArrayList<>();
        for (int left = count; left > 0; left -= perStack) {
            ItemStack stack = template.copy();
            stack.setCount(Math.min(left, perStack));
            stacks.add(stack);
        }
        return stacks;
    }

    /** Into the inventory; what does not fit drops at the player's feet, owned by them, with no pickup delay. */
    public static void give(EntityPlayer player, List<ItemStack> stacks) {
        boolean received = false;
        for (ItemStack stack : stacks) {
            int before = stack.getCount();
            player.inventory.addItemStackToInventory(stack);
            if (stack.getCount() < before) {
                received = true;
            }
            if (!stack.isEmpty()) {
                EntityItem dropped = player.dropItem(stack, false);
                if (dropped != null) {
                    dropped.setNoPickupDelay();
                    dropped.setOwner(player.getName());
                }
            }
        }
        if (received) {
            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP,
                    SoundCategory.PLAYERS, 0.2F,
                    ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            player.inventoryContainer.detectAndSendChanges();
        }
    }
}
