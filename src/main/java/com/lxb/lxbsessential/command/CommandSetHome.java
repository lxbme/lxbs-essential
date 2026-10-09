package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.data.Names;
import com.lxb.lxbsessential.data.PlayerData;
import com.lxb.lxbsessential.util.Locations;
import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public final class CommandSetHome extends PlayerCommand {

    @Override
    public String getName() {
        return "sethome";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/sethome [name]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        String name = args.length == 0 ? Names.DEFAULT_HOME : args[0];
        String error = Names.validate(name);
        if (error != null) {
            player.sendMessage(Messages.error(error));
            return;
        }
        if (ModConfig.isDimensionBlocked(player.dimension)) {
            player.sendMessage(Messages.cannotSetHere());
            return;
        }
        PlayerData data = Storage.players().get(player.getUniqueID());
        int max = ModConfig.homes.maxHomes;
        switch (data.setHome(name, Locations.of(player), max)) {
            case LIMIT_REACHED:
                player.sendMessage(Messages.homeLimit(max));
                return;
            case OVERWRITTEN:
                player.sendMessage(Messages.homeUpdated(name));
                break;
            default:
                player.sendMessage(Messages.homeSet(name));
                break;
        }
        Storage.savePlayer(player);
    }
}
