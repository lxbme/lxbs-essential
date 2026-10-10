package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.data.NamedLocation;
import com.lxb.lxbsessential.data.PlayerData;
import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public final class CommandHomes extends PlayerCommand {

    @Override
    public String getName() {
        return "homes";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/homes";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        PlayerData data = Storage.players().get(player.getUniqueID());
        if (data.homeCount() == 0) {
            player.sendMessage(Messages.noHomes());
            return;
        }
        player.sendMessage(Messages.homesHeader(data.homeCount(), ModConfig.homes.maxHomes));
        for (NamedLocation home : data.listHomes()) {
            player.sendMessage(Messages.listEntry(home, "/home"));
        }
    }
}
