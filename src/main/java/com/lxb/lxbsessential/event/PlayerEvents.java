package com.lxb.lxbsessential.event;

import com.lxb.lxbsessential.Tags;
import com.lxb.lxbsessential.util.Storage;
import net.minecraftforge.fml.common.Mod;
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
        if (Storage.isOpen()) {
            Storage.flushAndUnload(id);
        }
    }
}
