package com.lxb.lxbsessential.info;

import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.Collection;

/** Applies attribute modifiers in the same order as vanilla's ModifiableAttributeInstance. */
final class AttributeMath {

    private AttributeMath() {
    }

    static double value(double base, Collection<AttributeModifier> modifiers) {
        double sum = base;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == 0) {
                sum += modifier.getAmount();
            }
        }
        double result = sum;
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == 1) {
                result += sum * modifier.getAmount();
            }
        }
        for (AttributeModifier modifier : modifiers) {
            if (modifier.getOperation() == 2) {
                result *= 1 + modifier.getAmount();
            }
        }
        return result;
    }

    static double fromStack(ItemStack stack, EntityEquipmentSlot slot, IAttribute attribute, double base) {
        return value(base, stack.getAttributeModifiers(slot).get(attribute.getName()));
    }

    static boolean has(ItemStack stack, EntityEquipmentSlot slot, IAttribute attribute) {
        return stack.getAttributeModifiers(slot).containsKey(attribute.getName());
    }
}
