package com.lxb.lxbsessential.fix;

import net.minecraft.inventory.EntityEquipmentSlot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Which equipment slots /fix repairs. */
public enum FixTarget {
    HAND(EntityEquipmentSlot.MAINHAND),
    OFFHAND(EntityEquipmentSlot.OFFHAND),
    ARMOR(EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET),
    ALL(EntityEquipmentSlot.values());

    private final List<EntityEquipmentSlot> slots;

    FixTarget(EntityEquipmentSlot... slots) {
        this.slots = Collections.unmodifiableList(Arrays.asList(slots));
    }

    public List<EntityEquipmentSlot> slots() {
        return slots;
    }

    /** Returns null for an unknown name. */
    public static FixTarget parse(String name) {
        for (FixTarget target : values()) {
            if (target.name().equals(name.toUpperCase(Locale.ROOT))) {
                return target;
            }
        }
        return null;
    }
}
