package com.lxb.lxbsessential.info;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Optional First Aid integration for armor: First Aid lets each piece protect only its own body part,
 * scales armor per slot and can count enchantments per piece. Only read-only methods are called.
 */
public final class FirstAidArmor {

    public static final String ARMOR_UTILS = "ichttt.mods.firstaid.common.util.ArmorUtils";
    public static final String CONFIG = "ichttt.mods.firstaid.FirstAidConfig";
    static final String LOCAL_MODE = "LOCAL_ENCHANTMENTS";

    private final Method getArmor;
    private final Method getToughness;
    private final Method applyArmor;
    private final Method applyToughness;
    private final Method localEpf;
    private final Field enchantmentHandling;

    private FirstAidArmor(Method getArmor, Method getToughness, Method applyArmor, Method applyToughness,
                          Method localEpf, Field enchantmentHandling) {
        this.getArmor = getArmor;
        this.getToughness = getToughness;
        this.applyArmor = applyArmor;
        this.applyToughness = applyToughness;
        this.localEpf = localEpf;
        this.enchantmentHandling = enchantmentHandling;
    }

    /** Returns null when First Aid is missing or its API does not match. */
    @Nullable
    public static FirstAidArmor bind(String armorUtilsClass, String configClass) {
        try {
            ClassLoader loader = FirstAidArmor.class.getClassLoader();
            Class<?> utils = Class.forName(armorUtilsClass, false, loader);
            Class<?> config = Class.forName(configClass, false, loader);
            Method getArmor = utils.getMethod("getArmor", ItemStack.class, EntityEquipmentSlot.class, boolean.class);
            Method getToughness = utils.getMethod("getArmorToughness",
                    ItemStack.class, EntityEquipmentSlot.class, boolean.class);
            Method applyArmor = utils.getMethod("applyArmorModifier", EntityEquipmentSlot.class, double.class);
            Method applyToughness = utils.getMethod("applyToughnessModifier", EntityEquipmentSlot.class, double.class);
            Method localEpf = utils.getDeclaredMethod("calculateEnchantmentModifierLocal",
                    ItemStack.class, DamageSource.class);
            localEpf.setAccessible(true);
            Field handling = config.getField("enchantmentHandling");
            for (Method method : new Method[]{getArmor, getToughness, applyArmor, applyToughness, localEpf}) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    return null;
                }
            }
            if (!Modifier.isStatic(handling.getModifiers())) {
                return null;
            }
            return new FirstAidArmor(getArmor, getToughness, applyArmor, applyToughness, localEpf, handling);
        } catch (ReflectiveOperationException | LinkageError | SecurityException e) {
            return null;
        }
    }

    /** Armor this piece gives its body part, with Quality Tools quality and First Aid's slot multiplier. */
    public double armor(ItemStack stack, EntityEquipmentSlot slot) throws ReflectiveOperationException {
        double raw = (Double) getArmor.invoke(null, stack, slot, true);
        return (Double) applyArmor.invoke(null, slot, raw);
    }

    public double toughness(ItemStack stack, EntityEquipmentSlot slot) throws ReflectiveOperationException {
        double raw = (Double) getToughness.invoke(null, stack, slot, true);
        return (Double) applyToughness.invoke(null, slot, raw);
    }

    /** This piece's EPF against the source, or null when First Aid counts enchantments over the whole set. */
    @Nullable
    public Integer epf(ItemStack stack, DamageSource source) throws ReflectiveOperationException {
        Object handling = enchantmentHandling.get(null);
        if (handling == null) {
            return null;
        }
        Field modeField = handling.getClass().getField("armorEnchantmentMode");
        modeField.setAccessible(true);
        Object mode = modeField.get(handling);
        if (!(mode instanceof Enum) || !LOCAL_MODE.equals(((Enum<?>) mode).name())) {
            return null;
        }
        return (Integer) localEpf.invoke(null, stack, source);
    }
}
