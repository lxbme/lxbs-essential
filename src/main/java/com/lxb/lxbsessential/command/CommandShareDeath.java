package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.data.Location;
import com.lxb.lxbsessential.death.DeathLog;
import com.lxb.lxbsessential.util.Messages;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/** Broadcasts the player's last death location; each death can be shared once. */
public final class CommandShareDeath extends PlayerCommand {

    @Override
    public String getName() {
        return "sharedeath";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sharedeath";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if (!ModConfig.death.showCoordinates || !DeathLog.INSTANCE.hasDeath(player.getUniqueID())) {
            player.sendMessage(Messages.noDeathToShare());
            return;
        }
        Location location = DeathLog.INSTANCE.share(player.getUniqueID());
        if (location == null) {
            player.sendMessage(Messages.deathAlreadyShared());
            return;
        }
        server.getPlayerList().sendMessage(Messages.deathShared(player.getName(), location));
    }
}
