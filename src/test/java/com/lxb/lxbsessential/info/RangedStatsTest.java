package com.lxb.lxbsessential.info;

import com.lxb.lxbsessential.info.fake.FakeBrokenLongbow;
import com.lxb.lxbsessential.info.fake.FakeLongbow;
import com.lxb.lxbsessential.info.fake.FakeOtherBow;
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

class RangedStatsTest {

    private static final String LONGBOW = "com.lxb.lxbsessential.info.fake.FakeLongbow";
    private static final String BROKEN_LONGBOW = "com.lxb.lxbsessential.info.fake.FakeBrokenLongbow";

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    /** ceil(3 * 2) = 6; a crit adds nextInt(6 / 2 + 2), i.e. up to 4. */
    @Test
    void vanillaBowWithoutPower() {
        RangedStats stats = RangedStats.of(new ItemStack(Items.BOW), null);

        assertTrue(stats.hasDamage);
        assertEquals(6, stats.minHit);
        assertEquals(10, stats.maxHit);
        assertEquals(1.0, stats.drawSeconds, 1e-9);
        assertFalse(stats.estimate);
    }

    /** Power V: 2 + 5 * 0.5 + 0.5 = 5 damage, ceil(3 * 5) = 15, crit adds up to 8. */
    @Test
    void powerFiveBow() {
        ItemStack bow = new ItemStack(Items.BOW);
        bow.addEnchantment(Enchantments.POWER, 5);
        RangedStats stats = RangedStats.of(bow, null);

        assertEquals(15, stats.minHit);
        assertEquals(23, stats.maxHit);
        assertEquals(5, stats.power);
    }

    /** Speed 1.5: ceil(3 * 1.5 * 2) = 9, crit adds up to 9 / 2 + 1 = 5; 30 ticks = 1.5 s. */
    @Test
    void spartanLongbowUsesItsOwnSpeedAndDrawTime() {
        SpartanBridge spartan = SpartanBridge.bind(LONGBOW);
        assertNotNull(spartan);
        RangedStats stats = RangedStats.of(new ItemStack(new FakeLongbow()), spartan);

        assertEquals(9, stats.minHit);
        assertEquals(14, stats.maxHit);
        assertEquals(1.5, stats.drawSeconds, 1e-9);
        assertFalse(stats.estimate);
    }

    @Test
    void longbowWithoutTheBridgeIsAVanillaEstimate() {
        RangedStats stats = RangedStats.of(new ItemStack(new FakeLongbow()), null);

        assertEquals(6, stats.minHit);
        assertEquals(10, stats.maxHit);
        assertTrue(stats.estimate);
    }

    @Test
    void unreadableLongbowFallsBackToAVanillaEstimate() {
        SpartanBridge spartan = SpartanBridge.bind(BROKEN_LONGBOW);
        assertNotNull(spartan);
        RangedStats stats = RangedStats.of(new ItemStack(new FakeBrokenLongbow()), spartan);

        assertEquals(6, stats.minHit);
        assertEquals(10, stats.maxHit);
        assertEquals(1.0, stats.drawSeconds, 1e-9);
        assertTrue(stats.estimate);
    }

    @Test
    void otherModsBowsAreVanillaEstimates() {
        RangedStats stats = RangedStats.of(new ItemStack(new FakeOtherBow()), SpartanBridge.bind(LONGBOW));

        assertEquals(6, stats.minHit);
        assertTrue(stats.estimate);
    }

    /** A crossbow is not an ItemBow: its bolts decide the damage, so only enchantments are explained. */
    @Test
    void nonBowWithBowEnchantmentsOnlyExplainsThem() {
        ItemStack crossbow = new ItemStack(Items.STICK);
        crossbow.addEnchantment(Enchantments.POWER, 5);
        crossbow.addEnchantment(Enchantments.FLAME, 1);

        assertTrue(RangedStats.appliesTo(crossbow));
        RangedStats stats = RangedStats.of(crossbow, null);
        assertFalse(stats.hasDamage);
        assertEquals(5, stats.power);
        assertTrue(stats.flame);
        assertFalse(stats.infinity);
    }

    @Test
    void swordIsNotRanged() {
        assertFalse(RangedStats.appliesTo(new ItemStack(Items.DIAMOND_SWORD)));
    }

    @Test
    void missingSpartanIsNotBindable() {
        assertNull(SpartanBridge.bind("com.lxb.lxbsessential.info.fake.NoSuchClass"));
    }
}
