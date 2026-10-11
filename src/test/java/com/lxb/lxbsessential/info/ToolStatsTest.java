package com.lxb.lxbsessential.info;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolStatsTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    private static Item modTool(String toolClass, int level) {
        return new Item() {
            @Override
            public Set<String> getToolClasses(ItemStack stack) {
                return Collections.singleton(toolClass);
            }

            @Override
            public int getHarvestLevel(ItemStack stack, String type, EntityPlayer player, IBlockState state) {
                return level;
            }
        };
    }

    @Test
    void diamondPickaxeOnStone() {
        ToolStats stats = ToolStats.of(new ItemStack(Items.DIAMOND_PICKAXE));

        assertEquals(1, stats.entries.size());
        ToolStats.Entry pickaxe = stats.entries.get(0);
        assertEquals("pickaxe", pickaxe.toolClass);
        assertEquals(3, pickaxe.harvestLevel);
        assertEquals("stone", pickaxe.referenceBlock);
        assertEquals(8.0F, pickaxe.digSpeed, 1e-6);
    }

    /** Same as the player's dig speed: Efficiency adds level squared plus one. */
    @Test
    void efficiencyFiveAddsTwentySix() {
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        pickaxe.addEnchantment(Enchantments.EFFICIENCY, 5);

        assertEquals(34.0F, ToolStats.of(pickaxe).entries.get(0).digSpeed, 1e-6);
    }

    @Test
    void axeAndShovelUseTheirOwnReferenceBlocks() {
        ToolStats.Entry axe = ToolStats.of(new ItemStack(Items.DIAMOND_AXE)).entries.get(0);
        ToolStats.Entry shovel = ToolStats.of(new ItemStack(Items.DIAMOND_SHOVEL)).entries.get(0);

        assertEquals("oak log", axe.referenceBlock);
        assertEquals(8.0F, axe.digSpeed, 1e-6);
        assertEquals("dirt", shovel.referenceBlock);
        assertEquals(8.0F, shovel.digSpeed, 1e-6);
    }

    @Test
    void unknownToolClassShowsHarvestLevelOnly() {
        ToolStats.Entry hammer = ToolStats.of(new ItemStack(modTool("hammer", 2))).entries.get(0);

        assertEquals("hammer", hammer.toolClass);
        assertEquals(2, hammer.harvestLevel);
        assertNull(hammer.referenceBlock);
        assertTrue(Float.isNaN(hammer.digSpeed));
    }

    @Test
    void unknownToolClassWithoutHarvestLevelIsSkipped() {
        assertTrue(ToolStats.of(new ItemStack(modTool("hammer", -1))).entries.isEmpty());
    }

    @Test
    void onlyItemsWithToolClassesCount() {
        assertTrue(ToolStats.appliesTo(new ItemStack(Items.DIAMOND_PICKAXE)));
        assertFalse(ToolStats.appliesTo(new ItemStack(Items.STICK)));
        assertFalse(ToolStats.appliesTo(new ItemStack(Items.DIAMOND_HOE)));
    }
}
