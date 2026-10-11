package com.lxb.lxbsessential.info;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeleeStatsTest {

    private static final double EPS = 1e-4;

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    private static ItemStack sword(Enchantment enchantment, int level) {
        ItemStack stack = new ItemStack(Items.DIAMOND_SWORD);
        stack.addEnchantment(enchantment, level);
        return stack;
    }

    /** Same numbers as the vanilla tooltip: 1 base + 6 sword damage, 4 base - 2.4 sword speed. */
    @Test
    void diamondSwordWithSharpnessFive() {
        MeleeStats.Line item = MeleeStats.of(sword(Enchantments.SHARPNESS, 5), 7, 1.6).item;

        assertEquals(7.0, item.damage, EPS);
        assertEquals(1.6, item.speed, EPS);
        assertEquals(3.0, item.bonus, EPS);
        assertEquals(10.0, item.total(), EPS);
        assertEquals(13.5, item.crit(), EPS);
        assertEquals(16.0, item.dps(), EPS);
        assertFalse(item.undeadDiffers());
        assertFalse(item.arthropodsDiffer());
    }

    @Test
    void smiteOnlyRaisesDamageAgainstUndead() {
        MeleeStats.Line item = MeleeStats.of(sword(Enchantments.SMITE, 5), 7, 1.6).item;

        assertEquals(7.0, item.total(), EPS);
        assertTrue(item.undeadDiffers());
        assertEquals(19.5, item.vsUndead(), EPS);
        assertFalse(item.arthropodsDiffer());
    }

    /** The holder's attribute already includes the sword, potions and baubles; enchantments come on top. */
    @Test
    void holderLineUsesTheHoldersAttributesPlusEnchantments() {
        MeleeStats.Line holder = MeleeStats.of(sword(Enchantments.SHARPNESS, 5), 13, 1.2).holder;

        assertEquals(16.0, holder.total(), EPS);
        assertEquals(22.5, holder.crit(), EPS);
        assertEquals(19.2, holder.dps(), EPS);
    }

    @Test
    void onlyItemsWithAttackDamageCount() {
        assertTrue(MeleeStats.appliesTo(new ItemStack(Items.DIAMOND_SWORD)));
        assertTrue(MeleeStats.appliesTo(new ItemStack(Items.DIAMOND_PICKAXE)));
        assertFalse(MeleeStats.appliesTo(new ItemStack(Items.STICK)));
    }

    @Test
    void modifierOperationsFollowVanillaOrder() {
        java.util.List<net.minecraft.entity.ai.attributes.AttributeModifier> modifiers = java.util.Arrays.asList(
                new net.minecraft.entity.ai.attributes.AttributeModifier("add", 3, 0),
                new net.minecraft.entity.ai.attributes.AttributeModifier("base", 0.5, 1),
                new net.minecraft.entity.ai.attributes.AttributeModifier("total", 1.0, 2));
        // (1 + 3) = 4; + 4 * 0.5 = 6; * (1 + 1) = 12
        assertEquals(12.0, AttributeMath.value(1, modifiers), EPS);
    }
}
