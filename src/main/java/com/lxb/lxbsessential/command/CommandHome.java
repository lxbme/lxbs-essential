package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.data.NamedLocation;
import com.lxb.lxbsessential.data.Names;
import com.lxb.lxbsessential.teleport.TeleportService;
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

public final class CommandHome extends PlayerCommand {

    @Override
    public String getName() {
        return "home";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/home [name]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        String name = args.length == 0 ? Names.DEFAULT_HOME : args[0];
        NamedLocation home = Storage.players().get(player.getUniqueID()).getHome(name);
        if (home == null) {
            player.sendMessage(Messages.homeNotFound(name));
            return;
        }
        TeleportService.request(player, home.location, Messages.homeLabel(home.name));
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
