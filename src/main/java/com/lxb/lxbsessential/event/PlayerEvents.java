package com.lxb.lxbsessential.event;

import com.lxb.lxbsessential.Tags;
import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.death.DeathLog;
import com.lxb.lxbsessential.teleport.TeleportService;
import com.lxb.lxbsessential.data.Location;
import com.lxb.lxbsessential.util.Locations;
import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class PlayerEvents {

    private PlayerEvents() {
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (Storage.isOpen()) {
            Storage.players().get(event.player.getUniqueID());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID id = event.player.getUniqueID();
        TeleportService.forget(id);
        DeathLog.INSTANCE.forget(id);
        if (Storage.isOpen()) {
            Storage.flushAndUnload(id);
        }
    }

    /** Lowest priority and not receiving cancelled events: a death prevented by another mod is not recorded. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP) || event.getEntityLiving() instanceof FakePlayer) {
            return;
        }
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        TeleportService.forget(player.getUniqueID());
        if (ModConfig.death.showCoordinates) {
            DeathLog.INSTANCE.record(player.getUniqueID(), Locations.of(player));
        }
        if (!ModConfig.teleport.backOnDeath || !Storage.isOpen()) {
            return;
        }
        Storage.players().get(player.getUniqueID()).setBack(Locations.of(player));
        Storage.savePlayer(player);
    }

    /** Sent on respawn rather than on death: chat is hidden behind the death screen and fades out. */
    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered() || !ModConfig.death.showCoordinates) {
            return;
        }
        Location death = DeathLog.INSTANCE.takeNotice(event.player.getUniqueID());
        if (death != null) {
            event.player.sendMessage(Messages.deathNotice(death, ModConfig.teleport.backOnDeath));
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHurt(LivingHurtEvent event) {
        if (event.getAmount() > 0
                && event.getEntityLiving() instanceof EntityPlayerMP
                && !(event.getEntityLiving() instanceof FakePlayer)) {
            TeleportService.onDamaged((EntityPlayerMP) event.getEntityLiving());
        }
    }
}
