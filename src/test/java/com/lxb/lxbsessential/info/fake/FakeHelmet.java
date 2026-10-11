package com.lxb.lxbsessential.info.fake;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.UUID;

/** A mod helmet that is not an ItemArmor but gives armor through attributes in the head slot. */
public class FakeHelmet extends Item {

    private static final UUID ARMOR = UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150");

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack stack) {
        return EntityEquipmentSlot.HEAD;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> modifiers = HashMultimap.create();
        if (slot == EntityEquipmentSlot.HEAD) {
            modifiers.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR, "Fake armor", 3.0, 0));
        }
        return modifiers;
    }
}
