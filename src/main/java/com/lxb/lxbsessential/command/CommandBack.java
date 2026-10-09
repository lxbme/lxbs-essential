package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.data.Location;
import com.lxb.lxbsessential.teleport.TeleportService;
import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public final class CommandBack extends PlayerCommand {

    @Override
    public String getName() {
        return "back";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/back";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Location back = Storage.players().get(player.getUniqueID()).getBack();
        if (back == null) {
            player.sendMessage(Messages.noBack());
            return;
        }
        TeleportService.request(player, back, Messages.BACK_LABEL);
    }
}
