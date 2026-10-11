package com.lxb.lxbsessential.info;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/** Harvest level per tool class, and dig speed on a typical block for the common ones. */
public final class ToolStats {

    public final List<Entry> entries;

    private ToolStats(List<Entry> entries) {
        this.entries = Collections.unmodifiableList(entries);
    }

    public static boolean appliesTo(ItemStack stack) {
        return !stack.getItem().getToolClasses(stack).isEmpty();
    }

    public static ToolStats of(ItemStack stack) {
        List<Entry> entries = new ArrayList<>();
        for (String toolClass : new TreeSet<>(stack.getItem().getToolClasses(stack))) {
            int level = stack.getItem().getHarvestLevel(stack, toolClass, null, null);
            IBlockState reference = referenceBlock(toolClass);
            if (level < 0 && reference == null) {
                continue;
            }
            entries.add(new Entry(toolClass, level,
                    reference == null ? null : referenceName(toolClass),
                    reference == null ? Float.NaN : digSpeed(stack, reference)));
        }
        return new ToolStats(entries);
    }

    /** EntityPlayer.getDigSpeed for the item alone: Efficiency only helps where the tool is already faster. */
    static float digSpeed(ItemStack stack, IBlockState state) {
        float speed = stack.getDestroySpeed(state);
        int efficiency = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack);
        if (speed > 1.0F && efficiency > 0) {
            speed += efficiency * efficiency + 1;
        }
        return speed;
    }

    @Nullable
    private static IBlockState referenceBlock(String toolClass) {
        switch (toolClass) {
            case "pickaxe":
                return Blocks.STONE.getDefaultState();
            case "axe":
                return Blocks.LOG.getDefaultState();
            case "shovel":
                return Blocks.DIRT.getDefaultState();
            default:
                return null;
        }
    }

    private static String referenceName(String toolClass) {
        switch (toolClass) {
            case "pickaxe":
                return "stone";
            case "axe":
                return "oak log";
            default:
                return "dirt";
        }
    }

    public static final class Entry {

        public final String toolClass;
        /** -1 when the item reports none. */
        public final int harvestLevel;
        @Nullable
        public final String referenceBlock;
        /** NaN when there is no reference block. */
        public final float digSpeed;

        Entry(String toolClass, int harvestLevel, @Nullable String referenceBlock, float digSpeed) {
            this.toolClass = toolClass;
            this.harvestLevel = harvestLevel;
            this.referenceBlock = referenceBlock;
            this.digSpeed = digSpeed;
        }
    }
}
