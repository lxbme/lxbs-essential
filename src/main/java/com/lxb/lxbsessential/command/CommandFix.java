package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.fix.FixTarget;
import com.lxb.lxbsessential.fix.Repair;
import com.lxb.lxbsessential.util.Messages;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

/** Operator-only: durability is a core cost in RLCraft, so this is an admin tool for compensating players. */
public final class CommandFix extends CommandBase {

    @Override
    public String getName() {
        return "fix";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/fix [hand|offhand|armor|all] [player]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 2) {
            throw new WrongUsageException(getUsage(sender));
        }
        FixTarget target = args.length == 0 ? FixTarget.ALL : FixTarget.parse(args[0]);
        if (target == null) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = args.length == 2 ? getPlayer(server, sender, args[1]) : getCommandSenderAsPlayer(sender);

        int repaired = Repair.repair(player, target);
        if (repaired == 0) {
            sender.sendMessage(Messages.nothingToRepair());
            return;
        }
        sender.sendMessage(Messages.repaired(repaired, player.getName()));
        if (player != sender) {
            player.sendMessage(Messages.repairedByAdmin());
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "hand", "offhand", "armor", "all");
        }
        if (args.length == 2) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 1;
    }
}
