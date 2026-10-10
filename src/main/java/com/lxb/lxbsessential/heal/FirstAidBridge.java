package com.lxb.lxbsessential.heal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.capabilities.Capability;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Optional First Aid integration through reflection. First Aid splits health into body parts and derives
 * vanilla health from them, so a full heal has to restore every part.
 */
public final class FirstAidBridge {

    public static final String CAPABILITY_HOLDER = "ichttt.mods.firstaid.api.CapabilityExtendedHealthSystem";

    private static final Class<?>[] HEAL_ARGS = {float.class, EntityPlayer.class, boolean.class};

    private final Field capability;

    private FirstAidBridge(Field capability) {
        this.capability = capability;
    }

    /** Returns null when First Aid is missing or its API does not match. */
    public static FirstAidBridge bind(String capabilityHolderClass) {
        try {
            Field field = Reflect.load(capabilityHolderClass).getField("INSTANCE");
            return Modifier.isStatic(field.getModifiers()) ? new FirstAidBridge(field) : null;
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /** Returns false when the player has no First Aid data. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean healPlayer(EntityPlayer player) throws ReflectiveOperationException {
        Object value = capability.get(null);
        if (!(value instanceof Capability)) {
            return false;
        }
        Capability cap = (Capability) value;
        if (!player.hasCapability(cap, null)) {
            return false;
        }
        healModel(player.getCapability(cap, null), player);
        return true;
    }

    void healModel(Object model, EntityPlayer player) throws ReflectiveOperationException {
        for (Object part : (Iterable<?>) model) {
            float max = ((Number) Reflect.call(part, "getMaxHealth", new Class<?>[0])).floatValue();
            // applyDebuff=false: an admin heal should not trigger First Aid's healing side effects
            Reflect.call(part, "heal", HEAL_ARGS, max, player, false);
        }
        Reflect.call(model, "scheduleResync", new Class<?>[0]);
    }
}
