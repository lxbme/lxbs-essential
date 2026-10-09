package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.data.Names;
import com.lxb.lxbsessential.util.Locations;
import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/** Operator-only: warps are shared by everyone, so only admins may create or move them. */
public final class CommandSetWarp extends CommandBase {

    @Override
    public String getName() {
        return "setwarp";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/setwarp <name>";
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
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        String name = args[0];
        String error = Names.validate(name);
        if (error != null) {
            player.sendMessage(Messages.error(error));
            return;
        }
        if (ModConfig.isDimensionBlocked(player.dimension)) {
            player.sendMessage(Messages.cannotSetHere());
            return;
        }
        boolean overwritten = Storage.warps().put(name, Locations.of(player));
        player.sendMessage(overwritten ? Messages.warpUpdated(name) : Messages.warpSet(name));
        Storage.saveWarps(player);
    }
}
