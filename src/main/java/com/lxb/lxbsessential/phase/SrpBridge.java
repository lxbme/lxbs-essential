package com.lxb.lxbsessential.phase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Reads Scape and Run: Parasites evolution data through reflection, so SRP stays an optional dependency
 * and an SRP version with a different API disables /phase instead of crashing the server.
 * With SRPMixins' chunk phases, the reading is for the region at the player's position.
 */
public final class SrpBridge {

    public static final String SAVE_DATA = "com.dhanantry.scapeandrunparasites.world.SRPSaveData";
    public static final String COMMAND = "com.dhanantry.scapeandrunparasites.network.SRPCommandEvolution";
    public static final String CONFIG = "com.dhanantry.scapeandrunparasites.util.config.SRPConfigSystems";
    public static final String REGION_LOOKUP = "srpmixins.util.customphasemechanics.SRPSaveDataInterface";

    public static final class Reading {
        public final int phase;
        public final int points;
        /** Points needed to reach the next phase; 0 at the last phase and for phases below 0. */
        public final int nextThreshold;
        /** True when the data covers the region at the player's position rather than the whole dimension. */
        public final boolean region;

        Reading(int phase, int points, int nextThreshold, boolean region) {
            this.phase = phase;
            this.points = points;
            this.nextThreshold = nextThreshold;
            this.region = region;
        }
    }

    private final Method dimensionData;
    private final Method regionData;
    private final Method phase;
    private final Method points;
    private final Method neededPoints;
    private final Field useEvolution;

    private SrpBridge(Method dimensionData, Method regionData, Method phase, Method points, Method neededPoints,
                      Field useEvolution) {
        this.dimensionData = dimensionData;
        this.regionData = regionData;
        this.phase = phase;
        this.points = points;
        this.neededPoints = neededPoints;
        this.useEvolution = useEvolution;
    }

    /** Returns null when SRP is missing or its API does not match. The region lookup and config flag are optional. */
    public static SrpBridge bind(String saveDataClass, String commandClass, String configClass, String regionLookupClass) {
        try {
            Class<?> saveData = load(saveDataClass);
            Method dimensionData = staticMethod(saveData, "get", World.class);
            Method phase = saveData.getMethod("getEvolutionPhase", int.class);
            Method points = saveData.getMethod("getTotalKills", int.class);
            Method neededPoints = staticMethod(load(commandClass), "getNeededPoints", byte.class);
            return new SrpBridge(dimensionData, optionalRegionLookup(regionLookupClass), phase, points, neededPoints,
                    optionalFlag(configClass));
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    public boolean hasRegions() {
        return regionData != null;
    }

    public boolean evolutionEnabled() {
        try {
            return useEvolution == null || useEvolution.getBoolean(null);
        } catch (IllegalAccessException e) {
            return true;
        }
    }

    public Reading read(World world, EntityPlayer player, BlockPos pos, int dim) throws ReflectiveOperationException {
        Object dimensionWide = dimensionData.invoke(null, world);
        Object data = regionData == null ? dimensionWide : regionData.invoke(null, world, player, pos);
        int current = ((Number) phase.invoke(data, dim)).intValue();
        int total = ((Number) points.invoke(data, dim)).intValue();
        // SRP's thresholds are "points to reach phase N"; nothing is configured past the last phase.
        int next = current >= 0 ? ((Number) neededPoints.invoke(null, (byte) (current + 1))).intValue() : 0;
        return new Reading(current, total, next, data != dimensionWide);
    }

    private static Class<?> load(String name) throws ClassNotFoundException {
        return Class.forName(name, false, SrpBridge.class.getClassLoader());
    }

    private static Method staticMethod(Class<?> owner, String name, Class<?>... parameters) throws NoSuchMethodException {
        Method method = owner.getMethod(name, parameters);
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new NoSuchMethodException(owner.getName() + "." + name + " is not static");
        }
        return method;
    }

    private static Method optionalRegionLookup(String className) {
        try {
            return staticMethod(load(className), "get", World.class, EntityPlayer.class, BlockPos.class);
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }

    private static Field optionalFlag(String className) {
        try {
            Field field = load(className).getField("useEvolution");
            return Modifier.isStatic(field.getModifiers()) && field.getType() == boolean.class ? field : null;
        } catch (ReflectiveOperationException | LinkageError e) {
            return null;
        }
    }
}
