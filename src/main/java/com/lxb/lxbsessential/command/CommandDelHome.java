package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CommandDelHome extends PlayerCommand {

    @Override
    public String getName() {
        return "delhome";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/delhome <name>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if (!Storage.players().get(player.getUniqueID()).deleteHome(args[0])) {
            player.sendMessage(Messages.homeNotFound(args[0]));
            return;
        }
        player.sendMessage(Messages.homeDeleted(args[0]));
        Storage.savePlayer(player);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length != 1 || !(sender instanceof EntityPlayerMP)) {
            return Collections.emptyList();
        }
        List<String> names = Storage.players().get(((EntityPlayerMP) sender).getUniqueID()).listHomes().stream()
                .map(home -> home.name)
                .collect(Collectors.toList());
        return getListOfStringsMatchingLastWord(args, names);
    }
}
