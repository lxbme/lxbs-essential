package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/** Operator-only; also usable from the server console. */
public final class CommandDelWarp extends CommandBase {

    @Override
    public String getName() {
        return "delwarp";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/delwarp <name>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        if (!Storage.warps().remove(args[0])) {
            sender.sendMessage(Messages.warpNotFound(args[0]));
            return;
        }
        sender.sendMessage(Messages.warpDeleted(args[0]));
        Storage.saveWarps(sender);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length != 1) {
            return Collections.emptyList();
        }
        List<String> names = Storage.warps().list().stream().map(warp -> warp.name).collect(Collectors.toList());
        return getListOfStringsMatchingLastWord(args, names);
    }
}
