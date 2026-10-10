package com.lxb.lxbsessential.util;

import com.lxb.lxbsessential.data.Location;
import com.lxb.lxbsessential.data.NamedLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Every player-facing string. Clients do not have this mod's language files,
 * so only literal {@link TextComponentString}s are used.
 */
public final class Messages {

    public static final String BACK_LABEL = "your last location";

    private Messages() {
    }

    public static String homeLabel(String name) {
        return "home '" + name + "'";
    }

    public static String warpLabel(String name) {
        return "warp '" + name + "'";
    }

    public static ITextComponent error(String text) {
        return colored(text, TextFormatting.RED);
    }

    private static ITextComponent info(String text) {
        return colored(text, TextFormatting.GREEN);
    }

    private static ITextComponent colored(String text, TextFormatting color) {
        ITextComponent component = new TextComponentString(text);
        component.getStyle().setColor(color);
        return component;
    }

    // homes

    public static ITextComponent homeSet(String name) {
        return info("Home '" + name + "' set.");
    }

    public static ITextComponent homeUpdated(String name) {
        return info("Home '" + name + "' updated.");
    }

    public static ITextComponent homeLimit(int max) {
        return error("You already have " + max + " homes. Delete one with /delhome first.");
    }

    public static ITextComponent homeNotFound(String name) {
        return error("No home named '" + name + "'. Use /homes to list them.");
    }

    public static ITextComponent homeDeleted(String name) {
        return info("Home '" + name + "' deleted.");
    }

    public static ITextComponent homesHeader(int count, int max) {
        return info("Homes (" + count + "/" + max + "):");
    }

    public static ITextComponent noHomes() {
        return error("You have no homes. Set one with /sethome.");
    }

    // warps

    public static ITextComponent warpSet(String name) {
        return info("Warp '" + name + "' set.");
    }

    public static ITextComponent warpUpdated(String name) {
        return info("Warp '" + name + "' updated.");
    }

    public static ITextComponent warpNotFound(String name) {
        return error("No warp named '" + name + "'. Use /warps to list them.");
    }

    public static ITextComponent warpDeleted(String name) {
        return info("Warp '" + name + "' deleted.");
    }

    public static ITextComponent warpsHeader(int count) {
        return info("Warps (" + count + "):");
    }

    public static ITextComponent noWarps() {
        return error("There are no warps yet.");
    }

    // shared listing / back

    /** One line of /homes or /warps; clicking anywhere on it runs {@code command <name>}. */
    public static ITextComponent listEntry(NamedLocation entry, String command) {
        ITextComponent line = new TextComponentString(" - ");
        clickable(line, command + " " + entry.name, "Click to teleport");
        ITextComponent name = new TextComponentString(entry.name);
        name.getStyle().setUnderlined(true);
        line.appendSibling(name);
        line.appendSibling(new TextComponentString(": " + entry.location.describe()));
        return line;
    }

    private static void clickable(ITextComponent component, String command, String hoverText) {
        component.getStyle()
                .setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(hoverText)));
    }

    /**
     * Clicking puts the plain coordinates into the chat box (1.12 has no copy-to-clipboard action);
     * shift-clicking inserts them at the cursor of an open chat box.
     */
    private static ITextComponent coordinates(Location location) {
        String coords = location.blockCoords();
        ITextComponent component = new TextComponentString(location.describe());
        component.getStyle()
                .setUnderlined(true)
                .setInsertion(coords)
                .setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, coords))
                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new TextComponentString("Click to put the coordinates in your chat box")));
        return component;
    }

    private static ITextComponent button(String label, String command, String hoverText) {
        ITextComponent button = colored(label, TextFormatting.AQUA);
        clickable(button, command, hoverText);
        return button;
    }

    public static ITextComponent noBack() {
        return error("You have no location to go back to.");
    }

    // teleport

    public static ITextComponent warmup(String label, int seconds) {
        return info("Teleporting to " + label + " in " + seconds + "s. Don't move or take damage.");
    }

    public static ITextComponent countdown(int seconds) {
        return colored("Teleporting in " + seconds + "...", TextFormatting.GOLD);
    }

    public static ITextComponent cancelledMoved() {
        return error("Teleport cancelled: you moved.");
    }

    public static ITextComponent cancelledDamaged() {
        return error("Teleport cancelled: you took damage.");
    }

    public static ITextComponent cooldown(int seconds) {
        return error("You must wait " + seconds + "s before teleporting again.");
    }

    public static ITextComponent cannotSetHere() {
        return error("You can't set homes or warps in this dimension.");
    }

    public static ITextComponent dimensionBlockedHere() {
        return error("Teleporting is disabled in this dimension.");
    }

    public static ITextComponent dimensionBlockedTarget() {
        return error("Teleporting to that dimension is disabled.");
    }

    public static ITextComponent dimensionMissing(int dim) {
        return error("Dimension " + dim + " no longer exists.");
    }

    public static ITextComponent teleportFailed() {
        return error("Teleport failed, please contact an admin.");
    }

    public static ITextComponent teleported(String label) {
        return info("Teleported to " + label + ".");
    }

    // death

    public static ITextComponent deathNotice(Location location, boolean withBack) {
        ITextComponent notice = colored("You died at ", TextFormatting.GOLD);
        notice.appendSibling(coordinates(location));
        notice.appendSibling(new TextComponentString("."));
        if (withBack) {
            notice.appendSibling(new TextComponentString(" "));
            notice.appendSibling(button("[Back]", "/back", "Teleport back to where you died"));
        }
        notice.appendSibling(new TextComponentString(" "));
        notice.appendSibling(button("[Share]", "/sharedeath", "Tell everyone where you died"));
        return notice;
    }

    public static ITextComponent deathShared(String playerName, Location location) {
        ITextComponent message = colored(playerName + " died at ", TextFormatting.GOLD);
        message.appendSibling(coordinates(location));
        message.appendSibling(new TextComponentString("."));
        return message;
    }

    public static ITextComponent noDeathToShare() {
        return error("You have no recent death to share.");
    }

    public static ITextComponent deathAlreadyShared() {
        return error("You already shared this death.");
    }

    // parasites

    public static ITextComponent phaseReport(int phase, int points, int nextThreshold, boolean region) {
        if (phase <= -2) {
            return colored("Parasites cannot evolve here.", TextFormatting.GOLD);
        }
        String text = "Parasite phase here: " + phase + (region ? " (this region)" : " (this dimension)") + " - ";
        if (phase >= 0 && nextThreshold <= 0) {
            text += "maximum phase";
        } else {
            text += points + " / " + nextThreshold + " points to phase " + (phase + 1);
            if (phase >= 0) {
                long percent = Math.max(0, Math.min(100, points * 100L / nextThreshold));
                text += " (" + percent + "%)";
            }
        }
        return colored(text, TextFormatting.GOLD);
    }

    public static ITextComponent phaseUnavailable() {
        return error("Could not read parasite data.");
    }

    public static ITextComponent evolutionDisabled() {
        return error("Parasite evolution is disabled on this server.");
    }

    // fix

    public static ITextComponent repaired(int count, String playerName) {
        return info("Repaired " + count + " item(s) for " + playerName + ".");
    }

    public static ITextComponent nothingToRepair() {
        return error("Nothing to repair.");
    }

    public static ITextComponent repairedByAdmin() {
        return info("Your equipment was repaired by an admin.");
    }

    // heal

    public static ITextComponent healed(String playerName) {
        return info("Healed " + playerName + ".");
    }

    public static ITextComponent healedByAdmin() {
        return info("You were healed by an admin.");
    }

    // xp bank

    private static String percent(double value) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(value) + "%";
    }

    public static ITextComponent bankBalance(long balance, int levelsFromZero, long minutesToInterest) {
        String text = "Balance: " + balance + " XP (= level " + levelsFromZero + " from zero).";
        if (minutesToInterest >= 0) {
            text += " Next interest in " + minutesToInterest + (minutesToInterest == 1 ? " minute." : " minutes.");
        }
        return info(text);
    }

    public static List<ITextComponent> bankHelp(double ratePercent, int periodMinutes, long capPoints, int capLevel,
                                                double feePercent) {
        List<ITextComponent> lines = new ArrayList<>();
        lines.add(info("XP Bank commands:"));
        lines.add(new TextComponentString(" /xpbank balance - show your balance"));
        lines.add(new TextComponentString(" /xpbank deposit <levels|all> - store XP from your level bar"));
        lines.add(new TextComponentString(" /xpbank withdraw <levels|all> - take XP back"));
        lines.add(new TextComponentString(" /xpbank pay <player> <points> - send XP to another player"));
        if (ratePercent <= 0 || capPoints <= 0) {
            lines.add(info("Interest: none."));
        } else {
            lines.add(info("Interest: " + percent(ratePercent) + " every " + periodMinutes
                    + (periodMinutes == 1 ? " minute" : " minutes") + " you are online, on balances up to "
                    + capPoints + " XP (level " + capLevel + ")."));
        }
        lines.add(info(feePercent <= 0 ? "Withdrawals are free." : "Withdrawals cost a " + percent(feePercent) + " fee."));
        return lines;
    }

    public static ITextComponent deposited(long points, long balance) {
        return info("Deposited " + points + " XP. Balance: " + balance + " XP.");
    }

    public static ITextComponent withdrew(long received, long fee, long balance) {
        String feeText = fee > 0 ? " (fee " + fee + " XP)" : "";
        return info("Withdrew " + received + " XP" + feeText + ". Balance: " + balance + " XP.");
    }

    public static ITextComponent notEnoughLevels(int have) {
        return error("You only have " + have + (have == 1 ? " level." : " levels."));
    }

    public static ITextComponent nothingToDeposit() {
        return error("You have no XP to deposit.");
    }

    public static ITextComponent balanceEmpty() {
        return error("Your bank balance is empty.");
    }

    public static ITextComponent paid(long points, String to, long balance) {
        return info("Sent " + points + " XP to " + to + ". Balance: " + balance + " XP.");
    }

    public static ITextComponent paymentReceived(long points, String from) {
        return info(from + " sent you " + points + " XP.");
    }

    public static ITextComponent receivedWhileAway(long points) {
        return info("You received " + points + " XP while you were away.");
    }

    public static ITextComponent payInsufficient(long balance) {
        return error("You only have " + balance + " XP in the bank.");
    }

    public static ITextComponent paySelf() {
        return error("You can't pay yourself.");
    }

    public static ITextComponent unknownPlayer(String name) {
        return error("Player '" + name + "' has never played on this server.");
    }

    public static ITextComponent interestCredited(long credited, long balance) {
        return colored("+" + credited + " XP interest (balance: " + balance + ")", TextFormatting.GOLD);
    }

    public static ITextComponent adminBalance(String name, long balance) {
        return info(name + " has " + balance + " XP in the bank.");
    }

    public static ITextComponent adminSet(String name, long balance) {
        return info("Set " + name + "'s balance to " + balance + " XP.");
    }

    public static ITextComponent adminAdded(String name, long delta, long balance) {
        return info("Changed " + name + "'s balance by " + delta + " XP. New balance: " + balance + " XP.");
    }

    public static ITextComponent bankUnknownSubcommand() {
        return error("Unknown subcommand. Use /xpbank help.");
    }

    // storage / bed

    public static ITextComponent saveFailed() {
        return error("Failed to save, please contact an admin.");
    }

    public static ITextComponent spawnSet() {
        return info("Spawn point set.");
    }
}
