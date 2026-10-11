package com.lxb.lxbsessential.info;

import com.lxb.lxbsessential.info.fake.FakeFirstAidConfig;
import com.lxb.lxbsessential.info.fake.FakeHelmet;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorStatsTest {

    private static final String UTILS = "com.lxb.lxbsessential.info.fake.FakeArmorUtils";
    private static final String THROWING_UTILS = "com.lxb.lxbsessential.info.fake.FakeThrowingArmorUtils";
    private static final String CONFIG = "com.lxb.lxbsessential.info.fake.FakeFirstAidConfig";
    private static final double EPS = 1e-3;

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    @AfterEach
    void resetEnchantmentMode() {
        FakeFirstAidConfig.enchantmentHandling.armorEnchantmentMode =
                FakeFirstAidConfig.EnchantmentHandling.ArmorEnchantmentMode.LOCAL_ENCHANTMENTS;
    }

    private static ItemStack chestplate(Enchantment enchantment, int level) {
        ItemStack stack = new ItemStack(Items.DIAMOND_CHESTPLATE);
        stack.addEnchantment(enchantment, level);
        return stack;
    }

    private static void assertExample(ArmorStats.Example example, float damage, double afterArmor,
                                      double afterEnchantments) {
        assertEquals(damage, example.damage, EPS);
        assertEquals(afterArmor, example.afterArmor, EPS);
        assertEquals(afterEnchantments, example.afterEnchantments, EPS);
    }

    /** CombatRules: 10 → 10 * (1 - 4/25) = 8.4 → * (1 - 4/25) = 7.056; 30 → armor floor 1.6 → 28.08 → 23.587. */
    @Test
    void vanillaChestplateWithProtectionFour() {
        ArmorStats stats = ArmorStats.of(chestplate(Enchantments.PROTECTION, 4), null, false);

        assertEquals(EntityEquipmentSlot.CHEST, stats.slot);
        assertEquals(8.0, stats.armor, EPS);
        assertEquals(2.0, stats.toughness, EPS);
        assertEquals(4, (int) stats.epf.get(ArmorStats.Protection.GENERIC));
        assertEquals(4, (int) stats.epf.get(ArmorStats.Protection.FIRE));
        assertEquals(4, (int) stats.epf.get(ArmorStats.Protection.FALL));
        assertFalse(stats.firstAid);
        assertFalse(stats.estimate);
        assertEquals(2, stats.examples.size());
        assertExample(stats.examples.get(0), 10, 8.4, 7.056);
        assertExample(stats.examples.get(1), 30, 28.08, 23.587);
    }

    @Test
    void fireProtectionOnlyCountsAgainstFire() {
        ArmorStats stats = ArmorStats.of(chestplate(Enchantments.FIRE_PROTECTION, 4), null, false);

        assertNull(stats.epf.get(ArmorStats.Protection.GENERIC));
        assertEquals(8, (int) stats.epf.get(ArmorStats.Protection.FIRE));
        assertExample(stats.examples.get(0), 10, 8.4, 8.4);
    }

    /** Fake First Aid: armor (8 + 1 quality) * 2 = 18, toughness 3 + 1 = 4, local EPF 12. */
    @Test
    void firstAidLocalModeUsesItsArmorAndPerPieceEnchantments() {
        FirstAidArmor firstAid = FirstAidArmor.bind(UTILS, CONFIG);
        assertNotNull(firstAid);
        ArmorStats stats = ArmorStats.of(chestplate(Enchantments.PROTECTION, 4), firstAid, true);

        assertEquals(18.0, stats.armor, EPS);
        assertEquals(4.0, stats.toughness, EPS);
        assertEquals(12, (int) stats.epf.get(ArmorStats.Protection.GENERIC));
        assertTrue(stats.firstAid);
        assertFalse(stats.estimate);
        assertExample(stats.examples.get(0), 10, 4.1333, 2.1493);
        assertExample(stats.examples.get(1), 30, 20.4, 10.608);
    }

    @Test
    void firstAidGlobalModeKeepsVanillaEnchantments() {
        FakeFirstAidConfig.enchantmentHandling.armorEnchantmentMode =
                FakeFirstAidConfig.EnchantmentHandling.ArmorEnchantmentMode.GLOBAL_ENCHANTMENTS;
        ArmorStats stats = ArmorStats.of(chestplate(Enchantments.PROTECTION, 4), FirstAidArmor.bind(UTILS, CONFIG), true);

        assertEquals(18.0, stats.armor, EPS);
        assertEquals(4, (int) stats.epf.get(ArmorStats.Protection.GENERIC));
        assertExample(stats.examples.get(0), 10, 4.1333, 3.472);
    }

    @Test
    void firstAidFailureFallsBackToAVanillaEstimate() {
        FirstAidArmor firstAid = FirstAidArmor.bind(THROWING_UTILS, CONFIG);
        assertNotNull(firstAid);
        ArmorStats stats = ArmorStats.of(chestplate(Enchantments.PROTECTION, 4), firstAid, true);

        assertEquals(8.0, stats.armor, EPS);
        assertEquals(4, (int) stats.epf.get(ArmorStats.Protection.GENERIC));
        assertTrue(stats.firstAid);
        assertTrue(stats.estimate);
    }

    /** First Aid is installed but bind() returned null at startup: its API did not match. */
    @Test
    void firstAidInstalledButNotBoundIsAnEstimate() {
        ArmorStats stats = ArmorStats.of(chestplate(Enchantments.PROTECTION, 4), null, true);

        assertEquals(8.0, stats.armor, EPS);
        assertTrue(stats.firstAid);
        assertTrue(stats.estimate);
    }

    /** First Aid only reads ItemArmor, so other armor items get vanilla numbers marked as estimates. */
    @Test
    void nonItemArmorUnderFirstAidIsAnEstimate() {
        ArmorStats stats = ArmorStats.of(new ItemStack(new FakeHelmet()), FirstAidArmor.bind(UTILS, CONFIG), true);

        assertEquals(EntityEquipmentSlot.HEAD, stats.slot);
        assertEquals(3.0, stats.armor, EPS);
        assertTrue(stats.estimate);
    }

    @Test
    void onlyArmorItemsCount() {
        assertTrue(ArmorStats.appliesTo(new ItemStack(Items.DIAMOND_CHESTPLATE)));
        assertTrue(ArmorStats.appliesTo(new ItemStack(new FakeHelmet())));
        assertFalse(ArmorStats.appliesTo(new ItemStack(Blocks.PUMPKIN)));
        assertFalse(ArmorStats.appliesTo(new ItemStack(Items.SKULL)));
        assertFalse(ArmorStats.appliesTo(new ItemStack(Items.ELYTRA)));
        assertFalse(ArmorStats.appliesTo(new ItemStack(Items.DIAMOND_SWORD)));
    }

    @Test
    void missingFirstAidIsNotBindable() {
        assertNull(FirstAidArmor.bind("com.lxb.lxbsessential.info.fake.NoSuchClass", CONFIG));
        assertNull(FirstAidArmor.bind(UTILS, "com.lxb.lxbsessential.info.fake.NoSuchClass"));
    }
}
