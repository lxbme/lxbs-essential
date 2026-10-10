package com.lxb.lxbsessential.heal;

import net.minecraft.entity.player.EntityPlayer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** Optional SimpleDifficulty integration through reflection: thirst and body temperature. */
public final class SimpleDifficultyBridge {

    public static final String CAPABILITIES = "com.charles445.simpledifficulty.api.SDCapabilities";
    public static final String TEMPERATURE_ENUM = "com.charles445.simpledifficulty.api.temperature.TemperatureEnum";

    private static final int MAX_THIRST = 20;
    private static final float MAX_THIRST_SATURATION = 20F;

    private final Method thirstData;
    private final Method temperatureData;
    /** Middle of SimpleDifficulty's NORMAL temperature range, or -1 when unknown. */
    private final int normalTemperature;

    private SimpleDifficultyBridge(Method thirstData, Method temperatureData, int normalTemperature) {
        this.thirstData = thirstData;
        this.temperatureData = temperatureData;
        this.normalTemperature = normalTemperature;
    }

    /** Returns null when SimpleDifficulty is missing; temperature support is optional. */
    public static SimpleDifficultyBridge bind(String capabilitiesClass, String temperatureEnumClass) {
        try {
            Class<?> capabilities = Reflect.load(capabilitiesClass);
            Method thirst = staticGetter(capabilities, "getThirstData");
            Method temperature = optionalStaticGetter(capabilities, "getTemperatureData");
            return new SimpleDifficultyBridge(thirst, temperature, normalTemperature(temperatureEnumClass));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    /** Returns false when the player has no thirst data. */
    public boolean quench(EntityPlayer player) throws ReflectiveOperationException {
        Object data = thirstData.invoke(null, player);
        if (data == null) {
            return false;
        }
        Reflect.call(data, "setThirstLevel", new Class<?>[]{int.class}, MAX_THIRST);
        Reflect.call(data, "setThirstSaturation", new Class<?>[]{float.class}, MAX_THIRST_SATURATION);
        Reflect.call(data, "setThirstExhaustion", new Class<?>[]{float.class}, 0F);
        Reflect.call(data, "setThirstDamageCounter", new Class<?>[]{int.class}, 0);
        return true;
    }

    /** Returns false when temperature is unsupported or the player has no temperature data. */
    public boolean normalizeTemperature(EntityPlayer player) throws ReflectiveOperationException {
        if (temperatureData == null || normalTemperature < 0) {
            return false;
        }
        Object data = temperatureData.invoke(null, player);
        if (data == null) {
            return false;
        }
        Reflect.call(data, "setTemperatureLevel", new Class<?>[]{int.class}, normalTemperature);
        Reflect.call(data, "setTemperatureDamageCounter", new Class<?>[]{int.class}, 0);
        return true;
    }

    private static Method staticGetter(Class<?> owner, String name) throws NoSuchMethodException {
        Method method = owner.getMethod(name, EntityPlayer.class);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new NoSuchMethodException(owner.getName() + "." + name + " is not static");
        }
        return method;
    }

    private static Method optionalStaticGetter(Class<?> owner, String name) {
        try {
            return staticGetter(owner, name);
        } catch (NoSuchMethodException | LinkageError e) {
            return null;
        }
    }

    private static int normalTemperature(String temperatureEnumClass) {
        try {
            Object normal = Reflect.load(temperatureEnumClass).getField("NORMAL").get(null);
            return ((Number) Reflect.call(normal, "getMiddle", new Class<?>[0])).intValue();
        } catch (ReflectiveOperationException | LinkageError | RuntimeException e) {
            return -1;
        }
    }
}
