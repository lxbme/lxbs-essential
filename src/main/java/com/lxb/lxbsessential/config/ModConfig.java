package com.lxb.lxbsessential.config;

import com.lxb.lxbsessential.Tags;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** {@code config/lxbsessential.cfg}. On a dedicated server, edits take effect after a restart. */
@Config(modid = Tags.MOD_ID)
public final class ModConfig {

    @Config.Comment("Home settings")
    public static Homes homes = new Homes();

    @Config.Comment("Teleport settings shared by /home, /warp and /back")
    public static Teleport teleport = new Teleport();

    @Config.Comment("Right-click a bed to set your spawn point at any time of day")
    public static Bed bed = new Bed();

    @Config.Comment("Death location messages")
    public static Death death = new Death();

    @Config.Comment("Scape and Run: Parasites integration (only active when SRP is installed)")
    public static Parasites parasites = new Parasites();

    private ModConfig() {
    }

    public static boolean isDimensionBlocked(int dim) {
        for (int blocked : teleport.dimensionBlacklist) {
            if (blocked == dim) {
                return true;
            }
        }
        return false;
    }

    public static final class Homes {
        @Config.Comment("Maximum number of homes per player")
        @Config.RangeInt(min = 1, max = 100)
        public int maxHomes = 3;
    }

    public static final class Teleport {
        @Config.Comment("Seconds to stand still before teleporting (0 = instant)")
        @Config.RangeInt(min = 0, max = 60)
        public int warmupSeconds = 3;

        @Config.Comment("Seconds after a teleport before the player can teleport again")
        @Config.RangeInt(min = 0, max = 3600)
        public int cooldownSeconds = 10;

        @Config.Comment("Operators skip the warm-up and the cooldown")
        public boolean opsBypass = true;

        @Config.Comment("Remember where a player died so /back can return there")
        public boolean backOnDeath = true;

        @Config.Comment("Dimension IDs where homes/warps cannot be set and teleports cannot start or end")
        public int[] dimensionBlacklist = new int[0];
    }

    public static final class Bed {
        @Config.Comment("Enable setting the spawn point by right-clicking a bed")
        public boolean enabled = true;

        @Config.Comment("Bed blocks to ignore: exact ids like 'modid:name' or whole mods like 'modid:*'")
        public String[] blockBlacklist = {"comforts:*"};
    }

    public static final class Death {
        @Config.Comment("After respawning, tell the player where they died, with [Back] and [Share] buttons")
        public boolean showCoordinates = true;
    }

    public static final class Parasites {
        @Config.Comment("Let every player use /phase to see the parasite evolution phase where they stand")
        public boolean phaseCommand = true;
    }

    @Mod.EventBusSubscriber(modid = Tags.MOD_ID)
    public static final class Sync {
        private Sync() {
        }

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (Tags.MOD_ID.equals(event.getModID())) {
                ConfigManager.sync(Tags.MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
