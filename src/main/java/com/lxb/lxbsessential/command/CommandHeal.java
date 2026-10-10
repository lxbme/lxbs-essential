package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.heal.FirstAidBridge;
import com.lxb.lxbsessential.heal.Healing;
import com.lxb.lxbsessential.heal.SimpleDifficultyBridge;
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

/** Operator-only full heal; First Aid and SimpleDifficulty are handled when installed. */
public final class CommandHeal extends CommandBase {

    @Nullable
    private final FirstAidBridge firstAid;
    @Nullable
    private final SimpleDifficultyBridge simpleDifficulty;

    public CommandHeal(@Nullable FirstAidBridge firstAid, @Nullable SimpleDifficultyBridge simpleDifficulty) {
        this.firstAid = firstAid;
        this.simpleDifficulty = simpleDifficulty;
    }

    @Override
    public String getName() {
        return "heal";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/heal [player]";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = args.length == 1 ? getPlayer(server, sender, args[0]) : getCommandSenderAsPlayer(sender);
        Healing.heal(player, firstAid, simpleDifficulty);
        sender.sendMessage(Messages.healed(player.getName()));
        if (player != sender) {
            player.sendMessage(Messages.healedByAdmin());
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        return args.length == 1
                ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
                : Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return index == 0;
    }
}
