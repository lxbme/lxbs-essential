package com.lxb.lxbsessential.util;

import com.lxb.lxbsessential.LxbsEssential;
import com.lxb.lxbsessential.data.PlayerStore;
import com.lxb.lxbsessential.data.WarpStore;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

/** Holds the stores for the running server and turns save failures into log lines plus a chat message. */
public final class Storage {

    private static PlayerStore players;
    private static WarpStore warps;

    private Storage() {
    }

    public static void open(Path root) {
        players = new PlayerStore(root);
        warps = new WarpStore(root);
        warps.load();
    }

    public static void close() {
        players = null;
        warps = null;
    }

    public static boolean isOpen() {
        return players != null;
    }

    public static PlayerStore players() {
        return players;
    }

    public static WarpStore warps() {
        return warps;
    }

    /** Changes stay in memory on failure, so the next successful save still persists them. */
    public static void savePlayer(EntityPlayerMP player) {
        try {
            players.save(player.getUniqueID());
        } catch (IOException e) {
            LxbsEssential.LOGGER.error("Failed to save data for {}", player.getName(), e);
            player.sendMessage(Messages.saveFailed());
        }
    }

    public static void saveWarps(ICommandSender sender) {
        try {
            warps.save();
        } catch (IOException e) {
            LxbsEssential.LOGGER.error("Failed to save warps", e);
            sender.sendMessage(Messages.saveFailed());
        }
    }

    public static void flushAndUnload(UUID id) {
        try {
            players.save(id);
        } catch (IOException e) {
            LxbsEssential.LOGGER.error("Failed to save data for {}", id, e);
        }
        players.unload(id);
    }
}
