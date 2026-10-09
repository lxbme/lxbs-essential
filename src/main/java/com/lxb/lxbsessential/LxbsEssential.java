package com.lxb.lxbsessential;

import com.lxb.lxbsessential.command.CommandDelHome;
import com.lxb.lxbsessential.command.CommandHome;
import com.lxb.lxbsessential.command.CommandHomes;
import com.lxb.lxbsessential.command.CommandSetHome;
import com.lxb.lxbsessential.teleport.TeleportService;
import com.lxb.lxbsessential.util.Storage;
import net.minecraftforge.common.DimensionManager;
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
    }

    @Mod.EventHandler
    public void onServerStopped(FMLServerStoppedEvent event) {
        TeleportService.reset();
        Storage.close();
    }
}
