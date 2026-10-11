package com.lxb.lxbsessential.info;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Protection of one armor piece. With First Aid each piece guards only its own body part, so the damage
 * examples apply to that part; without it vanilla adds the whole set, so they show this piece alone.
 */
public final class ArmorStats {

    static final float[] EXAMPLE_DAMAGE = {10F, 30F};

    public final EntityEquipmentSlot slot;
    public final double armor;
    public final double toughness;
    /** First Aid is installed, so the examples are per body part. */
    public final boolean firstAid;
    /** First Aid is installed but its numbers could not be used; these are vanilla numbers. */
    public final boolean estimate;
    /** Enchantment protection factor per damage type; only types above 0, in enum order. */
    public final Map<Protection, Integer> epf;
    public final List<Example> examples;

    private ArmorStats(EntityEquipmentSlot slot, double armor, double toughness, boolean firstAid, boolean estimate,
                       Map<Protection, Integer> epf) {
        this.slot = slot;
        this.armor = armor;
        this.toughness = toughness;
        this.firstAid = firstAid;
        this.estimate = estimate;
        this.epf = Collections.unmodifiableMap(epf);
        Integer generic = epf.get(Protection.GENERIC);
        List<Example> examples = new ArrayList<>();
        for (float damage : EXAMPLE_DAMAGE) {
            float afterArmor = CombatRules.getDamageAfterAbsorb(damage, (float) armor, (float) toughness);
            float afterEnchantments = generic == null
                    ? afterArmor
                    : CombatRules.getDamageAfterMagicAbsorb(afterArmor, generic);
            examples.add(new Example(damage, afterArmor, afterEnchantments));
        }
        this.examples = Collections.unmodifiableList(examples);
    }

    /** Pumpkins, skulls and elytra go in armor slots but give no armor, so they do not count. */
    public static boolean appliesTo(ItemStack stack) {
        EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
        if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR) {
            return false;
        }
        return stack.getItem() instanceof ItemArmor
                || AttributeMath.has(stack, slot, SharedMonsterAttributes.ARMOR)
                || AttributeMath.has(stack, slot, SharedMonsterAttributes.ARMOR_TOUGHNESS);
    }

    public static ArmorStats of(ItemStack stack, @Nullable FirstAidArmor firstAid, boolean firstAidInstalled) {
        EntityEquipmentSlot slot = EntityLiving.getSlotForItemStack(stack);
        double armor = AttributeMath.fromStack(stack, slot, SharedMonsterAttributes.ARMOR, 0);
        double toughness = AttributeMath.fromStack(stack, slot, SharedMonsterAttributes.ARMOR_TOUGHNESS, 0);
        Map<Protection, Integer> epf = vanillaEpf(stack);
        boolean estimate = firstAidInstalled;
        if (firstAid != null && stack.getItem() instanceof ItemArmor) {
            try {
                double firstAidArmor = firstAid.armor(stack, slot);
                double firstAidToughness = firstAid.toughness(stack, slot);
                Map<Protection, Integer> localEpf = firstAidEpf(stack, firstAid);
                armor = firstAidArmor;
                toughness = firstAidToughness;
                if (localEpf != null) {
                    epf = localEpf;
                }
                estimate = false;
            } catch (ReflectiveOperationException e) {
                estimate = true;
            }
        }
        return new ArmorStats(slot, armor, toughness, firstAidInstalled, estimate, epf);
    }

    private static Map<Protection, Integer> vanillaEpf(ItemStack stack) {
        Map<Protection, Integer> epf = new EnumMap<>(Protection.class);
        for (Protection protection : Protection.values()) {
            int value = EnchantmentHelper.getEnchantmentModifierDamage(
                    Collections.singleton(stack), protection.source());
            if (value > 0) {
                epf.put(protection, value);
            }
        }
        return epf;
    }

    /** Null when First Aid adds enchantments over the whole set like vanilla. */
    @Nullable
    private static Map<Protection, Integer> firstAidEpf(ItemStack stack, FirstAidArmor firstAid)
            throws ReflectiveOperationException {
        Map<Protection, Integer> epf = new EnumMap<>(Protection.class);
        for (Protection protection : Protection.values()) {
            Integer value = firstAid.epf(stack, protection.source());
            if (value == null) {
                return null;
            }
            if (value > 0) {
                epf.put(protection, value);
            }
        }
        return epf;
    }

    public enum Protection {
        GENERIC("normal"), FIRE("fire"), EXPLOSION("explosion"), PROJECTILE("projectile"), FALL("fall");

        public final String label;

        Protection(String label) {
            this.label = label;
        }

        /** A source that each protection enchantment recognises; built on demand, sources are cheap. */
        DamageSource source() {
            switch (this) {
                case FIRE:
                    return DamageSource.IN_FIRE;
                case EXPLOSION:
                    return new DamageSource("explosion").setExplosion();
                case PROJECTILE:
                    return new DamageSource("arrow").setProjectile();
                case FALL:
                    return DamageSource.FALL;
                default:
                    return DamageSource.GENERIC;
            }
        }
    }

    public static final class Example {

        public final float damage;
        public final float afterArmor;
        public final float afterEnchantments;

        Example(float damage, float afterArmor, float afterEnchantments) {
            this.damage = damage;
            this.afterArmor = afterArmor;
            this.afterEnchantments = afterEnchantments;
        }
    }
}
