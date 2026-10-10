package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.data.NamedLocation;
import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;

import java.util.List;

public final class CommandWarps extends PlayerCommand {

    @Override
    public String getName() {
        return "warps";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/warps";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        List<NamedLocation> warps = Storage.warps().list();
        if (warps.isEmpty()) {
            sender.sendMessage(Messages.noWarps());
            return;
        }
        sender.sendMessage(Messages.warpsHeader(warps.size()));
        for (NamedLocation warp : warps) {
            sender.sendMessage(Messages.listEntry(warp, "/warp"));
        }
    }
}
