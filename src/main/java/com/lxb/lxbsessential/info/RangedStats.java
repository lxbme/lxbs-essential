package com.lxb.lxbsessential.info;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

import javax.annotation.Nullable;

/**
 * Arrow damage with a normal arrow at full draw, point blank, using vanilla's arrow formula. Arrows from
 * other mods and on-hit effects can change the real number.
 */
public final class RangedStats {

    /** Vanilla bows launch at 3.0 times this multiplier after a 20-tick draw. */
    static final float VANILLA_SPEED = 1.0F;
    static final int VANILLA_DRAW_TICKS = 20;
    private static final double LAUNCH_VELOCITY = 3.0;
    private static final double ARROW_BASE_DAMAGE = 2.0;

    public final boolean hasDamage;
    public final int minHit;
    public final int maxHit;
    public final double drawSeconds;
    /** True when the numbers come from the vanilla formula for a bow that may behave differently. */
    public final boolean estimate;
    public final int power;
    public final int punch;
    public final boolean flame;
    public final boolean infinity;

    private RangedStats(boolean hasDamage, int minHit, int maxHit, double drawSeconds, boolean estimate,
                        int power, int punch, boolean flame, boolean infinity) {
        this.hasDamage = hasDamage;
        this.minHit = minHit;
        this.maxHit = maxHit;
        this.drawSeconds = drawSeconds;
        this.estimate = estimate;
        this.power = power;
        this.punch = punch;
        this.flame = flame;
        this.infinity = infinity;
    }

    public static boolean appliesTo(ItemStack stack) {
        return stack.getItem() instanceof ItemBow
                || level(Enchantments.POWER, stack) > 0
                || level(Enchantments.PUNCH, stack) > 0
                || level(Enchantments.FLAME, stack) > 0
                || level(Enchantments.INFINITY, stack) > 0;
    }

    public static RangedStats of(ItemStack stack, @Nullable SpartanBridge spartan) {
        int power = level(Enchantments.POWER, stack);
        int punch = level(Enchantments.PUNCH, stack);
        boolean flame = level(Enchantments.FLAME, stack) > 0;
        boolean infinity = level(Enchantments.INFINITY, stack) > 0;
        Item item = stack.getItem();
        if (!(item instanceof ItemBow)) {
            return new RangedStats(false, 0, 0, 0, false, power, punch, flame, infinity);
        }

        float speed = VANILLA_SPEED;
        int drawTicks = VANILLA_DRAW_TICKS;
        boolean estimate = item.getClass() != ItemBow.class;
        if (spartan != null && spartan.isLongbow(item)) {
            try {
                speed = spartan.maxArrowSpeed(item);
                drawTicks = spartan.drawTicks(item);
                estimate = false;
            } catch (ReflectiveOperationException e) {
                speed = VANILLA_SPEED;
                drawTicks = VANILLA_DRAW_TICKS;
            }
        }

        double damage = ARROW_BASE_DAMAGE + (power > 0 ? power * 0.5 + 0.5 : 0);
        int hit = MathHelper.ceil(LAUNCH_VELOCITY * speed * damage);
        // a critical arrow adds rand.nextInt(hit / 2 + 2), i.e. 0 .. hit / 2 + 1
        return new RangedStats(true, hit, hit + hit / 2 + 1, drawTicks / 20.0, estimate,
                power, punch, flame, infinity);
    }

    private static int level(Enchantment enchantment, ItemStack stack) {
        return EnchantmentHelper.getEnchantmentLevel(enchantment, stack);
    }
}
