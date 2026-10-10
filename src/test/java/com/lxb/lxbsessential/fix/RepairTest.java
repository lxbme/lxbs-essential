package com.lxb.lxbsessential.fix;

import net.minecraft.init.Bootstrap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepairTest {

    private static Item tool;
    private static Item stick;

    /** ItemStack needs Minecraft's registries, which the game normally sets up at startup. */
    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
        tool = new Item().setMaxDamage(100);
        stick = new Item();
    }

    private static ItemStack damaged(int damage) {
        ItemStack stack = new ItemStack(tool);
        stack.setItemDamage(damage);
        return stack;
    }

    @Test
    void damagedItemIsRestoredToFullDurability() {
        ItemStack stack = damaged(60);
        assertTrue(Repair.repair(stack));
        assertEquals(0, stack.getItemDamage());
    }

    /** Enchantments and mod data such as SRP armor adaptation live in NBT and must survive the repair. */
    @Test
    void repairKeepsNbt() {
        ItemStack stack = damaged(60);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("AdaptationCap", 7);
        stack.setTagCompound(tag);

        Repair.repair(stack);
        assertEquals(7, stack.getTagCompound().getInteger("AdaptationCap"));
    }

    @Test
    void undamagedItemCountsAsNotRepaired() {
        assertFalse(Repair.repair(damaged(0)));
    }

    @Test
    void itemsWithoutDurabilityAreLeftAlone() {
        assertFalse(Repair.repair(new ItemStack(stick)));
        assertFalse(Repair.repair(ItemStack.EMPTY));
    }
}
