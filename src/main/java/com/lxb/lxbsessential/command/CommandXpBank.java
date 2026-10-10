package com.lxb.lxbsessential.command;

import com.lxb.lxbsessential.LxbsEssential;
import com.lxb.lxbsessential.bank.Bank;
import com.lxb.lxbsessential.bank.BankService;
import com.lxb.lxbsessential.bank.Fees;
import com.lxb.lxbsessential.bank.Ledger;
import com.lxb.lxbsessential.bank.PlayerXp;
import com.lxb.lxbsessential.bank.XpMath;
import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.util.Messages;
import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** /xpbank (alias /xb): deposit, withdraw (with a fee), pay, and operator-only admin subcommands. */
public final class CommandXpBank extends PlayerCommand {

    private static final int MAX_INPUT = 1_000_000_000;
    private static final String[] PLAYER_SUBCOMMANDS = {"balance", "deposit", "withdraw", "pay", "help"};

    @Override
    public String getName() {
        return "xpbank";
    }

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("xb");
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/xpbank help";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String sub = args.length == 0 ? "balance" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "balance":
                balance(sender, args);
                break;
            case "help":
                for (ITextComponent line : Messages.bankHelp(ModConfig.bank.interestRatePercent,
                        ModConfig.bank.interestPeriodMinutes, ModConfig.bank.interestCapPoints,
                        XpMath.fromPoints(ModConfig.bank.interestCapPoints).level, ModConfig.bank.withdrawalFeePercent)) {
                    sender.sendMessage(line);
                }
                break;
            case "deposit":
                deposit(sender, args);
                break;
            case "withdraw":
                withdraw(sender, args);
                break;
            case "pay":
                pay(server, sender, args);
                break;
            case "admin":
                admin(server, sender, args);
                break;
            default:
                sender.sendMessage(Messages.bankUnknownSubcommand());
        }
    }

    private static BankService bank() {
        return Bank.service();
    }

    private static boolean interestActive() {
        return ModConfig.bank.interestRatePercent > 0 && ModConfig.bank.interestCapPoints > 0;
    }

    private void balance(ICommandSender sender, String[] args) throws CommandException {
        if (args.length > 1) {
            throw new WrongUsageException("/xpbank balance");
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Ledger ledger = bank().ledger();
        long balance = ledger.balance(player.getUniqueID());
        long minutes = interestActive()
                ? (ledger.ticksUntilInterest(player.getUniqueID(), ModConfig.bankPeriodTicks()) + 1199) / 1200
                : -1;
        player.sendMessage(Messages.bankBalance(balance, XpMath.fromPoints(balance).level, minutes));
    }

    private void deposit(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new WrongUsageException("/xpbank deposit <levels|all>");
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        UUID id = player.getUniqueID();
        int level = player.experienceLevel;
        float progress = player.experience;
        long points;
        int newLevel;
        float newProgress;
        if ("all".equalsIgnoreCase(args[1])) {
            points = XpMath.total(level, progress);
            newLevel = 0;
            newProgress = 0F;
        } else {
            int levels = parseInt(args[1], 1, MAX_INPUT);
            if (levels > level) {
                player.sendMessage(Messages.notEnoughLevels(level));
                return;
            }
            points = XpMath.pointsBetween(level - levels, level, progress);
            newLevel = level - levels;
            newProgress = progress;
        }
        if (points <= 0) {
            player.sendMessage(Messages.nothingToDeposit());
            return;
        }
        long amount = points;
        try {
            long balance = bank().commit(ledger -> {
                ledger.deposit(id, player.getName(), amount);
                return ledger.balance(id);
            });
            PlayerXp.set(player, newLevel, newProgress);
            player.sendMessage(Messages.deposited(amount, balance));
        } catch (IOException e) {
            saveFailed(player, e);
        }
    }

    private void withdraw(ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 2) {
            throw new WrongUsageException("/xpbank withdraw <levels|all>");
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        UUID id = player.getUniqueID();
        long balance = bank().ledger().balance(id);
        if (balance <= 0) {
            player.sendMessage(Messages.balanceEmpty());
            return;
        }
        double feePercent = ModConfig.bank.withdrawalFeePercent;
        int level = player.experienceLevel;
        float progress = player.experience;
        long cost = balance;
        if (!"all".equalsIgnoreCase(args[1])) {
            int levels = parseInt(args[1], 1, MAX_INPUT);
            long target = (long) level + levels;
            if (target <= XpMath.MAX_LEVEL) {
                long needed = XpMath.pointsBetween(level, (int) target, progress);
                // the cost is never below what is needed, so only work it out when the balance could cover it
                if (needed < balance) {
                    cost = Math.min(balance, Fees.costFor(needed, feePercent));
                }
            }
        }
        long amount = cost;
        try {
            long taken = bank().commit(ledger -> ledger.take(id, amount));
            long received = Fees.received(taken, feePercent);
            XpMath.Xp xp = XpMath.fromPoints(Ledger.addCapped(PlayerXp.total(player), received));
            PlayerXp.set(player, xp.level, xp.progress);
            player.sendMessage(Messages.withdrew(received, taken - received, bank().ledger().balance(id)));
        } catch (IOException e) {
            saveFailed(player, e);
        }
    }

    private void pay(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length != 3) {
            throw new WrongUsageException("/xpbank pay <player> <points>");
        }
        EntityPlayerMP player = getCommandSenderAsPlayer(sender);
        Target target = resolve(server, sender, args[1]);
        if (target == null) {
            player.sendMessage(Messages.unknownPlayer(args[1]));
            return;
        }
        long points = parseLong(args[2], 1, MAX_INPUT);
        UUID from = player.getUniqueID();
        try {
            Ledger.PayResult result = bank().commit(ledger ->
                    ledger.pay(from, target.id, target.name, points, target.online != null));
            switch (result) {
                case SELF:
                    player.sendMessage(Messages.paySelf());
                    break;
                case INSUFFICIENT:
                    player.sendMessage(Messages.payInsufficient(bank().ledger().balance(from)));
                    break;
                case OK:
                    player.sendMessage(Messages.paid(points, target.name, bank().ledger().balance(from)));
                    if (target.online != null) {
                        target.online.sendMessage(Messages.paymentReceived(points, player.getName()));
                    }
                    break;
                default:
                    throw new WrongUsageException("/xpbank pay <player> <points>");
            }
        } catch (IOException e) {
            saveFailed(player, e);
        }
    }

    private void admin(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!sender.canUseCommand(2, getName())) {
            throw new CommandException("commands.generic.permission");
        }
        if (args.length < 3) {
            throw new WrongUsageException("/xpbank admin <balance|set|add> <player> [points]");
        }
        String action = args[1].toLowerCase(Locale.ROOT);
        Target target = resolve(server, sender, args[2]);
        if (target == null) {
            sender.sendMessage(Messages.unknownPlayer(args[2]));
            return;
        }
        if ("balance".equals(action) && args.length == 3) {
            sender.sendMessage(Messages.adminBalance(target.name, bank().ledger().balance(target.id)));
            return;
        }
        if (args.length != 4 || !("set".equals(action) || "add".equals(action))) {
            throw new WrongUsageException("/xpbank admin <balance|set|add> <player> [points]");
        }
        long points = parseLong(args[3], -MAX_INPUT, MAX_INPUT);
        try {
            if ("set".equals(action)) {
                long balance = bank().commit(ledger -> ledger.setBalance(target.id, target.name, points));
                sender.sendMessage(Messages.adminSet(target.name, balance));
            } else {
                long balance = bank().commit(ledger -> ledger.addBalance(target.id, target.name, points));
                sender.sendMessage(Messages.adminAdded(target.name, points, balance));
            }
        } catch (IOException e) {
            LxbsEssential.LOGGER.error("Failed to save bank.json", e);
            sender.sendMessage(Messages.saveFailed());
        }
    }

    private static void saveFailed(EntityPlayerMP player, IOException e) {
        LxbsEssential.LOGGER.error("Failed to save bank.json for {}", player.getName(), e);
        player.sendMessage(Messages.saveFailed());
    }

    private static final class Target {
        final UUID id;
        final String name;
        @Nullable
        final EntityPlayerMP online;

        Target(UUID id, String name, @Nullable EntityPlayerMP online) {
            this.id = id;
            this.name = name;
            this.online = online;
        }
    }

    /**
     * Single-player selectors (@p, @r) first, then UUIDs (vanilla expands @a in username slots into UUIDs),
     * then online players, names recorded in the bank, and the server's profile cache.
     */
    @Nullable
    private static Target resolve(MinecraftServer server, ICommandSender sender, String name) {
        if (EntitySelector.isSelector(name)) {
            try {
                EntityPlayerMP selected = EntitySelector.matchOnePlayer(sender, name);
                return selected == null ? null : new Target(selected.getUniqueID(), selected.getName(), selected);
            } catch (CommandException e) {
                return null;
            }
        }
        UUID uuid = uuidOrNull(name);
        if (uuid != null) {
            EntityPlayerMP byUuid = server.getPlayerList().getPlayerByUUID(uuid);
            if (byUuid != null) {
                return new Target(uuid, byUuid.getName(), byUuid);
            }
            String known = bank().ledger().name(uuid);
            if (!known.isEmpty()) {
                return new Target(uuid, known, null);
            }
            GameProfile cachedProfile = server.getPlayerProfileCache().getProfileByUUID(uuid);
            return cachedProfile == null ? null : new Target(uuid, cachedProfile.getName(), null);
        }
        EntityPlayerMP online = server.getPlayerList().getPlayerByUsername(name);
        if (online != null) {
            return new Target(online.getUniqueID(), online.getName(), online);
        }
        UUID known = bank().ledger().findByName(name);
        if (known != null) {
            return new Target(known, bank().ledger().name(known), null);
        }
        String cached = cachedName(server.getPlayerProfileCache().getUsernames(), name);
        if (cached != null) {
            GameProfile profile = server.getPlayerProfileCache().getGameProfileForUsername(cached);
            if (profile != null && profile.getId() != null) {
                return new Target(profile.getId(), profile.getName(), null);
            }
        }
        return null;
    }

    @Nullable
    static UUID uuidOrNull(String text) {
        try {
            return UUID.fromString(text);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Looks the name up among cached profiles only: on a cache miss getGameProfileForUsername would invent an
     * offline profile (offline mode) or make a blocking request to Mojang on the server thread (online mode).
     */
    @Nullable
    static String cachedName(String[] cachedUsernames, String name) {
        for (String cached : cachedUsernames) {
            if (cached.equalsIgnoreCase(name)) {
                return cached;
            }
        }
        return null;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        boolean admin = sender.canUseCommand(2, getName());
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            Collections.addAll(options, PLAYER_SUBCOMMANDS);
            if (admin) {
                options.add("admin");
            }
            return getListOfStringsMatchingLastWord(args, options);
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && ("deposit".equals(sub) || "withdraw".equals(sub))) {
            return getListOfStringsMatchingLastWord(args, "all");
        }
        if (args.length == 2 && "pay".equals(sub)) {
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        }
        if (admin && "admin".equals(sub)) {
            if (args.length == 2) {
                return getListOfStringsMatchingLastWord(args, "balance", "set", "add");
            }
            if (args.length == 3) {
                return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        if (args.length == 0) {
            return false;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        return ("pay".equals(sub) && index == 1) || ("admin".equals(sub) && index == 2);
    }
}
