package com.lxb.lxbsessential;

import com.lxb.lxbsessential.command.CommandBack;
import com.lxb.lxbsessential.command.CommandDelHome;
import com.lxb.lxbsessential.command.CommandDelWarp;
import com.lxb.lxbsessential.command.CommandFix;
import com.lxb.lxbsessential.command.CommandHome;
import com.lxb.lxbsessential.command.CommandHomes;
import com.lxb.lxbsessential.command.CommandPhase;
import com.lxb.lxbsessential.command.CommandSetHome;
import com.lxb.lxbsessential.command.CommandSetWarp;
import com.lxb.lxbsessential.command.CommandShareDeath;
import com.lxb.lxbsessential.command.CommandWarp;
import com.lxb.lxbsessential.command.CommandWarps;
import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.death.DeathLog;
import com.lxb.lxbsessential.phase.SrpBridge;
import com.lxb.lxbsessential.teleport.TeleportService;
import com.lxb.lxbsessential.util.Storage;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod(modid = Tags.MOD_ID, name = Tags.MOD_NAME, version = Tags.VERSION, acceptableRemoteVersions = "*")
public class LxbsEssential {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        Path root = DimensionManager.getCurrentSaveRootDirectory().toPath().resolve(Tags.MOD_ID);
        Storage.open(root);
        LOGGER.info("Data directory: {}", root);

        event.registerServerCommand(new CommandSetHome());
        event.registerServerCommand(new CommandHome());
        event.registerServerCommand(new CommandDelHome());
        event.registerServerCommand(new CommandHomes());
        event.registerServerCommand(new CommandWarp());
        event.registerServerCommand(new CommandWarps());
        event.registerServerCommand(new CommandSetWarp());
        event.registerServerCommand(new CommandDelWarp());
        event.registerServerCommand(new CommandBack());
        event.registerServerCommand(new CommandShareDeath());
        event.registerServerCommand(new CommandFix());
        registerPhaseCommand(event);
    }

    /** /phase only exists when Scape and Run: Parasites is installed and its API matches what SrpBridge expects. */
    private static void registerPhaseCommand(FMLServerStartingEvent event) {
        if (!ModConfig.parasites.phaseCommand || !Loader.isModLoaded("srparasites")) {
            return;
        }
        SrpBridge bridge = SrpBridge.bind(SrpBridge.SAVE_DATA, SrpBridge.COMMAND, SrpBridge.CONFIG, SrpBridge.REGION_LOOKUP);
        if (bridge == null) {
            LOGGER.warn("Scape and Run: Parasites is installed but its API does not match this version; /phase is disabled");
            return;
        }
        event.registerServerCommand(new CommandPhase(bridge));
        LOGGER.info("/phase enabled ({})", bridge.hasRegions() ? "regional phases via SRPMixins" : "dimension-wide phases");
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        TeleportService.reset();
        DeathLog.INSTANCE.clear();
        Storage.close();
    }
}
