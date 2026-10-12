package com.lxb.lxbsessential.saveditem;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemGiverTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @Test
    void splitsIntoFullStacksAndARemainder() {
        ItemStack template = new ItemStack(Items.STICK, 5);
        template.setStackDisplayName("Kindling");
        List<ItemStack> stacks = ItemGiver.stacks(template, 100);

        assertEquals(2, stacks.size());
        assertEquals(64, stacks.get(0).getCount());
        assertEquals(36, stacks.get(1).getCount());
        for (ItemStack stack : stacks) {
            assertEquals("Kindling", stack.getDisplayName());
            assertNotSame(template, stack);
        }
        assertNotSame(stacks.get(0).getTagCompound(), stacks.get(1).getTagCompound());
        assertEquals(5, template.getCount());
    }

    @Test
    void unstackableItemsComeOneEach() {
        List<ItemStack> stacks = ItemGiver.stacks(new ItemStack(Items.DIAMOND_SWORD), 3);

        assertEquals(3, stacks.size());
        for (ItemStack stack : stacks) {
            assertEquals(1, stack.getCount());
        }
    }

    @Test
    void atMostOneInventoryAtOnce() {
        assertEquals(36 * 64, ItemGiver.maxCount(new ItemStack(Items.STICK)));
        assertEquals(36, ItemGiver.maxCount(new ItemStack(Items.DIAMOND_SWORD)));
    }

    @Test
    void countOutsideTheRangeIsRejected() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        assertThrows(IllegalArgumentException.class, () -> ItemGiver.stacks(sword, 0));
        assertThrows(IllegalArgumentException.class, () -> ItemGiver.stacks(sword, 37));
    }
}
