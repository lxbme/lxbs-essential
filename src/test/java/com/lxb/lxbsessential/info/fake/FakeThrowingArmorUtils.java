package com.lxb.lxbsessential.info.fake;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

/** Same shape as ArmorUtils, but reading armor fails, as after an incompatible First Aid update. */
public final class FakeThrowingArmorUtils {

    private FakeThrowingArmorUtils() {
    }

    public static double getArmor(ItemStack stack, EntityEquipmentSlot slot, boolean withQuality) {
        throw new IllegalStateException("changed internals");
    }

    public static double getArmorToughness(ItemStack stack, EntityEquipmentSlot slot, boolean withQuality) {
        throw new IllegalStateException("changed internals");
    }

    public static double applyArmorModifier(EntityEquipmentSlot slot, double value) {
        return value;
    }

    public static double applyToughnessModifier(EntityEquipmentSlot slot, double value) {
        return value;
    }

    private static int calculateEnchantmentModifierLocal(ItemStack stack, DamageSource source) {
        throw new IllegalStateException("changed internals");
    }
}
