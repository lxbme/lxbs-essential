package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.info.Integrations;
import com.lxb.lxbsessential.info.ItemReport;
import com.lxb.lxbsessential.info.ShareCache;
import com.lxb.lxbsessential.util.ItemInfoText;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

/** Shows the stats of the main-hand item; [Share] runs "/info share" to broadcast that snapshot once. */
public final class CommandInfo extends PlayerCommand {

    private final Integrations integrations;

    public CommandInfo(Integrations integrations) {
        this.integrations = integrations;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/info";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 1 && "share".equals(args[0])) {
            share(server, getCommandSenderAsPlayer(sender));
            return;
        }
        if (args.length != 0) {
            throw new WrongUsageException(getUsage(sender));
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty()) {
            player.sendMessage(ItemInfoText.notHolding());
            return;
        }
        ItemReport report = ItemReport.build(stack, player.getName(), modName(stack),
                player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue(),
                player.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue(),
                integrations);
        ShareCache.REPORTS.put(player.getUniqueID(), report);
        for (ITextComponent line : ItemInfoText.report(report, false)) {
            player.sendMessage(line);
        }
    }

    private static void share(MinecraftServer server, EntityPlayerMP player) {
        ItemReport report = ShareCache.REPORTS.take(player.getUniqueID());
        if (report == null) {
            player.sendMessage(ItemInfoText.nothingToShare());
            return;
        }
        for (ITextComponent line : ItemInfoText.report(report, true)) {
            server.getPlayerList().sendMessage(line);
        }
    }

    private static String modName(ItemStack stack) {
        ResourceLocation id = stack.getItem().getRegistryName();
        if (id == null) {
            return "unknown";
        }
        ModContainer mod = Loader.instance().getIndexedModList().get(id.getNamespace());
        return mod != null ? mod.getName() : id.getNamespace();
    }
}
