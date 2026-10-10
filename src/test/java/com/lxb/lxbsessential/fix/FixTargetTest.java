package com.lxb.lxbsessential.fix;

import net.minecraft.inventory.EntityEquipmentSlot;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FixTargetTest {

    @Test
    void parsesEachTargetCaseInsensitively() {
        assertEquals(FixTarget.HAND, FixTarget.parse("hand"));
        assertEquals(FixTarget.OFFHAND, FixTarget.parse("OffHand"));
        assertEquals(FixTarget.ARMOR, FixTarget.parse("ARMOR"));
        assertEquals(FixTarget.ALL, FixTarget.parse("all"));
    }

    @Test
    void unknownTargetIsRejected() {
        assertNull(FixTarget.parse("boots"));
        assertNull(FixTarget.parse(""));
    }

    @Test
    void eachTargetCoversItsSlots() {
        assertEquals(Collections.singletonList(EntityEquipmentSlot.MAINHAND), FixTarget.HAND.slots());
        assertEquals(Collections.singletonList(EntityEquipmentSlot.OFFHAND), FixTarget.OFFHAND.slots());
        assertEquals(Arrays.asList(EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST,
                EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET), FixTarget.ARMOR.slots());
        assertEquals(Arrays.asList(EntityEquipmentSlot.values()), FixTarget.ALL.slots());
    }
}
