package com.lxb.lxbsessential.teleport;

import com.lxb.lxbsessential.LxbsEssential;
import com.lxb.lxbsessential.Tags;
import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.data.Location;
import com.lxb.lxbsessential.util.Locations;
import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Warm-up, cooldown and the actual move for /home, /warp and /back. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class TeleportService {

    private static final Map<UUID, Pending> PENDING = new HashMap<>();
    private static final Cooldowns COOLDOWNS = new Cooldowns();
    private static long tick;

    private TeleportService() {
    }

    private static final class Pending {
        final Location target;
        final String label;
        final double startX;
        final double startZ;
        final int startDim;
        final long executeAt;
        int shownSeconds = -1;

        Pending(Location target, String label, double startX, double startZ, int startDim, long executeAt) {
            this.target = target;
            this.label = label;
            this.startX = startX;
            this.startZ = startZ;
            this.startDim = startDim;
            this.executeAt = executeAt;
        }
    }

    public static void request(EntityPlayerMP player, Location target, String label) {
        switch (TeleportChecks.check(player.dimension, target.dim,
                ModConfig::isDimensionBlocked, DimensionManager::isDimensionRegistered)) {
            case BLOCKED_HERE:
                player.sendMessage(Messages.dimensionBlockedHere());
                return;
            case BLOCKED_TARGET:
                player.sendMessage(Messages.dimensionBlockedTarget());
                return;
            case MISSING_TARGET:
                player.sendMessage(Messages.dimensionMissing(target.dim));
                return;
            default:
                break;
        }
        UUID id = player.getUniqueID();
        boolean bypass = ModConfig.teleport.opsBypass && isOp(player);
        if (!bypass) {
            long remaining = COOLDOWNS.remainingMillis(id, System.currentTimeMillis());
            if (remaining > 0) {
                player.sendMessage(Messages.cooldown(Cooldowns.toDisplaySeconds(remaining)));
                return;
            }
        }
        int warmup = bypass ? 0 : ModConfig.teleport.warmupSeconds;
        if (warmup <= 0) {
            PENDING.remove(id);
            execute(player, target, label, !bypass);
            return;
        }
        PENDING.put(id, new Pending(target, label, player.posX, player.posZ, player.dimension, tick + warmup * 20L));
        player.sendMessage(Messages.warmup(label, warmup));
    }

    public static void forget(UUID id) {
        PENDING.remove(id);
    }

    public static void onDamaged(EntityPlayerMP player) {
        if (PENDING.remove(player.getUniqueID()) != null) {
            player.sendMessage(Messages.cancelledDamaged());
        }
    }

    public static void reset() {
        PENDING.clear();
        COOLDOWNS.clear();
        tick = 0;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        tick++;
        if (PENDING.isEmpty()) {
            return;
        }
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return;
        }
        List<Runnable> due = new ArrayList<>();
        Iterator<Map.Entry<UUID, Pending>> it = PENDING.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Pending> entry = it.next();
            Pending pending = entry.getValue();
            EntityPlayerMP player = server.getPlayerList().getPlayerByUUID(entry.getKey());
            if (player == null) {
                it.remove();
                continue;
            }
            if (player.dimension != pending.startDim
                    || WarmupRules.movedTooFar(pending.startX, pending.startZ, player.posX, player.posZ)) {
                it.remove();
                player.sendMessage(Messages.cancelledMoved());
                continue;
            }
            if (tick >= pending.executeAt) {
                it.remove();
                due.add(() -> SafeRun.run(() -> execute(player, pending.target, pending.label, true), e -> {
                    LxbsEssential.LOGGER.error("Teleport of {} failed", player.getName(), e);
                    player.sendMessage(Messages.teleportFailed());
                }));
                continue;
            }
            int seconds = WarmupRules.secondsLeft(tick, pending.executeAt);
            if (seconds != pending.shownSeconds) {
                pending.shownSeconds = seconds;
                player.sendStatusMessage(Messages.countdown(seconds), true);
            }
        }
        for (Runnable run : due) {
            run.run();
        }
    }

    private static void execute(EntityPlayerMP player, Location target, String label, boolean applyCooldown) {
        MinecraftServer server = player.getServer();
        if (server == null || !DimensionManager.isDimensionRegistered(target.dim) || server.getWorld(target.dim) == null) {
            player.sendMessage(Messages.dimensionMissing(target.dim));
            return;
        }
        Location from = Locations.of(player);
        if (player.isPlayerSleeping()) {
            player.wakeUpPlayer(true, true, false);
        }
        if (player.isRiding()) {
            player.dismountRidingEntity();
        }
        if (player.dimension != target.dim) {
            // Vanilla portals refuse ridden players; a passenger cannot follow into another world.
            player.removePassengers();
            server.getPlayerList().transferPlayerToDimension(player, target.dim, new SafeTeleporter(target));
            // Vanilla changeDimension does this after the transfer; without it the client shows 0 XP.
            player.addExperienceLevel(0);
        }
        player.connection.setPlayerLocation(target.x, target.y, target.z, target.yaw, target.pitch);
        player.fallDistance = 0F;

        Storage.players().get(player.getUniqueID()).setBack(from);
        Storage.savePlayer(player);
        if (applyCooldown) {
            COOLDOWNS.start(player.getUniqueID(), System.currentTimeMillis(), ModConfig.teleport.cooldownSeconds);
        }
        player.sendMessage(Messages.teleported(label));
    }

    private static boolean isOp(EntityPlayerMP player) {
        MinecraftServer server = player.getServer();
        return server != null && server.getPlayerList().canSendCommands(player.getGameProfile());
    }
}
