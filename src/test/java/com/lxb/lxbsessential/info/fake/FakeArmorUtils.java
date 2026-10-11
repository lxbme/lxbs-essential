package com.lxb.lxbsessential.info.fake;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

/** Mirrors First Aid's ArmorUtils with easy-to-spot numbers. */
public final class FakeArmorUtils {

    private FakeArmorUtils() {
    }

    /** 8 from attributes, +1 from Quality Tools quality when asked for. */
    public static double getArmor(ItemStack stack, EntityEquipmentSlot slot, boolean withQuality) {
        return withQuality ? 9.0 : 8.0;
    }

    public static double getArmorToughness(ItemStack stack, EntityEquipmentSlot slot, boolean withQuality) {
        return withQuality ? 3.0 : 2.0;
    }

    public static double applyArmorModifier(EntityEquipmentSlot slot, double value) {
        return value * 2.0;
    }

    public static double applyToughnessModifier(EntityEquipmentSlot slot, double value) {
        return value + 1.0;
    }

    private static int calculateEnchantmentModifierLocal(ItemStack stack, DamageSource source) {
        return 12;
    }
}
