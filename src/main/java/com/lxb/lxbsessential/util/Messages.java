package com.lxb.lxbsessential.util;

import com.lxb.lxbsessential.data.Location;
import com.lxb.lxbsessential.data.NamedLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

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

    // storage / bed

    public static ITextComponent saveFailed() {
        return error("Failed to save, please contact an admin.");
    }

    public static ITextComponent spawnSet() {
        return info("Spawn point set.");
    }
}
