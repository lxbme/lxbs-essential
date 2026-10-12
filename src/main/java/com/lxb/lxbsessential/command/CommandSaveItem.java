package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.LxbsEssential;
import com.lxb.lxbsessential.data.Names;
import com.lxb.lxbsessential.saveditem.ItemCodec;
import com.lxb.lxbsessential.saveditem.ItemGiver;
import com.lxb.lxbsessential.saveditem.SavedItem;
import com.lxb.lxbsessential.saveditem.SavedItemStore;
import com.lxb.lxbsessential.util.ItemInfoText;
import com.lxb.lxbsessential.util.Messages;
import com.lxb.lxbsessential.util.Storage;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Operator library of exact item copies: save, update, delete, list and give. */
public final class CommandSaveItem extends CommandBase {

    private static final String USAGE = "/si <save|update|delete|give|list> ...";

    @Override
    public String getName() {
        return "saveitem";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("si");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return USAGE;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 0) {
            throw new WrongUsageException(USAGE);
        }
        SavedItemStore store = Storage.savedItems();
        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "save":
                requireLength(args, 2, 2);
                save(sender, store, args[1], false);
                break;
            case "update":
                requireLength(args, 2, 2);
                save(sender, store, args[1], true);
                break;
            case "delete":
                requireLength(args, 2, 2);
                delete(sender, store, args[1]);
                break;
            case "give":
                requireLength(args, 2, 4);
                give(server, sender, store, args);
                break;
            case "list":
                requireLength(args, 1, 1);
                list(sender, store);
                break;
            default:
                throw new WrongUsageException(USAGE);
        }
    }

    private static void requireLength(String[] args, int min, int max) throws WrongUsageException {
        if (args.length < min || args.length > max) {
            throw new WrongUsageException(USAGE);
        }
    }

    private static void save(ICommandSender sender, SavedItemStore store, String name, boolean update)
            throws CommandException {
        String invalid = Names.validate(name);
        if (invalid != null) {
            sender.sendMessage(Messages.error(invalid));
            return;
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        ItemStack held = player.getHeldItemMainhand();
        if (held.isEmpty()) {
            sender.sendMessage(ItemInfoText.notHolding());
            return;
        }
        SavedItem existing = store.get(name);
        if (!update && existing != null) {
            sender.sendMessage(Messages.savedItemExists(existing.name));
            return;
        }
        if (update && existing == null) {
            sender.sendMessage(Messages.noSavedItem(name));
            return;
        }
        String text;
        try {
            text = ItemCodec.encode(held);
        } catch (ItemCodec.NotExact e) {
            LxbsEssential.LOGGER.warn("Could not save {} exactly: {}", held.getItem().getRegistryName(), e.getMessage());
            sender.sendMessage(Messages.savedItemNotExact());
            return;
        }
        SavedItem entry = new SavedItem(name, text, player.getName(), player.getUniqueID().toString(),
                LocalDateTime.now().withNano(0).toString());
        try {
            store.put(entry);
        } catch (IOException e) {
            LxbsEssential.LOGGER.error("Failed to save saved items", e);
            sender.sendMessage(Messages.saveFailed());
            return;
        }
        sender.sendMessage(update ? Messages.savedItemUpdated(name, held) : Messages.savedItemSaved(name, held));
    }

    private static void delete(ICommandSender sender, SavedItemStore store, String name) {
        SavedItem existing = store.get(name);
        if (existing == null) {
            sender.sendMessage(Messages.noSavedItem(name));
            return;
        }
        try {
            store.remove(name);
        } catch (IOException e) {
            LxbsEssential.LOGGER.error("Failed to save saved items", e);
            sender.sendMessage(Messages.saveFailed());
            return;
        }
        sender.sendMessage(Messages.savedItemDeleted(existing.name));
    }

    private static void give(MinecraftServer server, ICommandSender sender, SavedItemStore store, String[] args)
            throws CommandException {
        SavedItem saved = store.get(args[1]);
        if (saved == null) {
            sender.sendMessage(Messages.noSavedItem(args[1]));
            return;
        }
        ItemStack template;
        try {
            template = ItemCodec.decode(saved.item);
        } catch (NBTException e) {
            LxbsEssential.LOGGER.error("Saved item '{}' has unreadable data", saved.name, e);
            sender.sendMessage(Messages.savedItemUnreadable(saved.name));
            return;
        }
        if (template.isEmpty()) {
            sender.sendMessage(Messages.savedItemMissing(saved.name));
            return;
        }
        EntityPlayerMP target = args.length >= 3 ? getPlayer(server, sender, args[2]) : getCommandSenderAsPlayer(sender);
        int count = args.length >= 4 ? parseInt(args[3], 1) : template.getCount();
        int max = ItemGiver.maxCount(template);
        if (count > max) {
            sender.sendMessage(Messages.tooMany(max));
            return;
        }
        ItemGiver.give(target, ItemGiver.stacks(template, count));
        sender.sendMessage(Messages.gave(count, template, target.getName()));
        if (target != sender) {
            target.sendMessage(Messages.received(count, template));
        }
    }

    private static void list(ICommandSender sender, SavedItemStore store) {
        List<SavedItem> items = store.list();
        if (items.isEmpty()) {
            sender.sendMessage(Messages.noSavedItems());
            return;
        }
        sender.sendMessage(Messages.savedItemsHeader(items.size()));
        for (SavedItem saved : items) {
            ItemStack stack;
            try {
                stack = ItemCodec.decode(saved.item);
            } catch (NBTException e) {
                stack = ItemStack.EMPTY;
            }
            sender.sendMessage(Messages.savedItemEntry(saved.name, stack, saved.savedBy));
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "save", "update", "delete", "give", "list");
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && ("update".equals(sub) || "delete".equals(sub) || "give".equals(sub))) {
            List<String> names = new ArrayList<>();
            for (SavedItem saved : Storage.savedItems().list()) {
                names.add(saved.name);
            }
            return getListOfStringsMatchingLastWord(args, names);
        }
        if (args.length == 3 && "give".equals(sub)) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return args.length > 0 && "give".equalsIgnoreCase(args[0]) && index == 2;
    }
}
