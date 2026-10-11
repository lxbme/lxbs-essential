package com.lxb.lxbsessential.info;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

/**
 * Melee numbers from attributes and enchantments. Effects other mods add on hit through events
 * (RLCombat, weapon traits, enchantment procs) cannot be known without attacking, so they are left out.
 */
public final class MeleeStats {

    /** EntityPlayer registers attack damage 1 and attack speed 4 as its base values. */
    static final double PLAYER_BASE_DAMAGE = 1.0;
    static final double PLAYER_BASE_SPEED = 4.0;

    public final Line item;
    public final Line holder;

    private MeleeStats(Line item, Line holder) {
        this.item = item;
        this.holder = holder;
    }

    public static boolean appliesTo(ItemStack stack) {
        return AttributeMath.has(stack, EntityEquipmentSlot.MAINHAND, SharedMonsterAttributes.ATTACK_DAMAGE);
    }

    /**
     * The item line is what the vanilla tooltip shows for a player with no other bonuses; the holder line
     * starts from the holder's current attribute values, which already include the held item.
     */
    public static MeleeStats of(ItemStack stack, double holderDamage, double holderSpeed) {
        Line item = new Line(stack,
                AttributeMath.fromStack(stack, EntityEquipmentSlot.MAINHAND,
                        SharedMonsterAttributes.ATTACK_DAMAGE, PLAYER_BASE_DAMAGE),
                AttributeMath.fromStack(stack, EntityEquipmentSlot.MAINHAND,
                        SharedMonsterAttributes.ATTACK_SPEED, PLAYER_BASE_SPEED));
        return new MeleeStats(item, new Line(stack, holderDamage, holderSpeed));
    }

    public static final class Line {

        /** Damage before enchantments. */
        public final double damage;
        public final double speed;
        public final double bonus;
        public final double undeadBonus;
        public final double arthropodBonus;

        Line(ItemStack stack, double damage, double speed) {
            this.damage = damage;
            this.speed = speed;
            this.bonus = EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEFINED);
            this.undeadBonus = EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.UNDEAD);
            this.arthropodBonus = EnchantmentHelper.getModifierForCreature(stack, EnumCreatureAttribute.ARTHROPOD);
        }

        public double total() {
            return damage + bonus;
        }

        /** Vanilla multiplies by 1.5 before it adds enchantment damage. */
        public double crit() {
            return damage * 1.5 + bonus;
        }

        public double dps() {
            return total() * speed;
        }

        public double vsUndead() {
            return damage + undeadBonus;
        }

        public double vsArthropods() {
            return damage + arthropodBonus;
        }

        public boolean undeadDiffers() {
            return undeadBonus != bonus;
        }

        public boolean arthropodsDiffer() {
            return arthropodBonus != bonus;
        }
    }
}
