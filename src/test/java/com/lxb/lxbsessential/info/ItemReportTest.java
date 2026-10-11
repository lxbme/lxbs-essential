package com.lxb.lxbsessential.info;

import com.lxb.lxbsessential.info.fake.FakeBrokenTool;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemReportTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    private static ItemReport report(ItemStack stack) {
        return ItemReport.build(stack, "Steve", "Minecraft", 7, 1.6, Integrations.NONE);
    }

    @Test
    void swordHasOnlyAMeleeSection() {
        ItemReport report = report(new ItemStack(Items.DIAMOND_SWORD));

        assertNotNull(report.melee);
        assertNull(report.ranged);
        assertNull(report.tools);
        assertNull(report.armor);
        assertFalse(report.shield);
    }

    @Test
    void sectionsStack() {
        ItemReport pickaxe = report(new ItemStack(Items.DIAMOND_PICKAXE));
        assertNotNull(pickaxe.melee);
        assertNotNull(pickaxe.tools);

        assertTrue(report(new ItemStack(Items.SHIELD)).shield);
        assertNotNull(report(new ItemStack(Items.BOW)).ranged);
        assertNotNull(report(new ItemStack(Items.DIAMOND_CHESTPLATE)).armor);
    }

    @Test
    void failingSectionDoesNotHideTheOthers() {
        ItemReport report = report(new ItemStack(new FakeBrokenTool(new IllegalStateException("boom"))));

        assertNotNull(report.melee);
        assertNull(report.tools);
    }

    @Test
    void linkageErrorInAModItemIsContained() {
        ItemReport report = report(new ItemStack(new FakeBrokenTool(new NoSuchMethodError("gone"))));

        assertNotNull(report.melee);
        assertNull(report.tools);
    }

    /** [Share] broadcasts what was in hand when /info ran, even if the item changed since. */
    @Test
    void reportKeepsASnapshotOfTheStack() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        ItemReport report = report(sword);
        sword.addEnchantment(Enchantments.SHARPNESS, 5);

        assertFalse(report.stack.isItemEnchanted());
        assertEquals(0.0, report.melee.item.bonus, 1e-9);
    }
}
