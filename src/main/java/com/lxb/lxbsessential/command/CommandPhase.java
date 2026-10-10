package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.LxbsEssential;
import com.lxb.lxbsessential.phase.SrpBridge;
import com.lxb.lxbsessential.util.Messages;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

/** Shows the parasite evolution phase where the player stands; only registered when SRP is installed. */
public final class CommandPhase extends PlayerCommand {

    private final SrpBridge bridge;

    public CommandPhase(SrpBridge bridge) {
        this.bridge = bridge;
    }

    @Override
    public String getName() {
        return "phase";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/phase";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        if (!bridge.evolutionEnabled()) {
            player.sendMessage(Messages.evolutionDisabled());
            return;
        }
        try {
            SrpBridge.Reading reading = bridge.read(player.world, player, player.getPosition(), player.dimension);
            player.sendMessage(Messages.phaseReport(reading.phase, reading.points, reading.nextThreshold, reading.region));
        } catch (Exception e) {
            LxbsEssential.LOGGER.warn("Could not read parasite data for {}", player.getName(), e);
            player.sendMessage(Messages.phaseUnavailable());
        }
    }
}
