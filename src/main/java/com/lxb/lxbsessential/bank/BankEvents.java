package com.lxb.lxbsessential.bank;

import com.lxb.lxbsessential.LxbsEssential;
import com.lxb.lxbsessential.Tags;
import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.util.Messages;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.io.IOException;
import java.util.UUID;

/** Counts online time for interest, announces transfers received while offline, and saves on logout. */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class BankEvents {

    private BankEvents() {
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !ModConfig.bank.enabled || !Bank.isOpen()) {
            return;
        }
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return;
        }
        Ledger ledger = Bank.service().ledger();
        boolean credited = false;
        for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
            UUID id = player.getUniqueID();
            long interest = ledger.tickOnline(id, player.getName(), ModConfig.bankPeriodTicks(),
                    ModConfig.bank.interestRatePercent, ModConfig.bank.interestCapPoints);
            if (interest > 0) {
                credited = true;
                player.sendStatusMessage(Messages.interestCredited(interest, ledger.balance(id)), true);
            }
        }
        if (credited) {
            Bank.saveQuietly();
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!ModConfig.bank.enabled || !Bank.isOpen()) {
            return;
        }
        UUID id = event.player.getUniqueID();
        String name = event.player.getName();
        try {
            long pending = Bank.service().commit(ledger -> {
                ledger.touch(id, name);
                return ledger.takePendingReceived(id);
            });
            if (pending > 0) {
                event.player.sendMessage(Messages.receivedWhileAway(pending));
            }
        } catch (IOException e) {
            LxbsEssential.LOGGER.error("Failed to save bank.json on login of {}", name, e);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (ModConfig.bank.enabled && Bank.isOpen()) {
            Bank.saveQuietly();
        }
    }
}
