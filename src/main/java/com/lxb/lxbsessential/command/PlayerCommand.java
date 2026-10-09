package com.lxb.lxbsessential.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Base for commands every player may use. Vanilla's canUseCommand only lets non-op players run
 * tell/help/me/trigger, so a permission level of 0 alone is not enough; checkPermission must be overridden.
 */
public abstract class PlayerCommand extends CommandBase {

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
