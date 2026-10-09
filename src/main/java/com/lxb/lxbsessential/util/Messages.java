package com.lxb.lxbsessential.util;

import com.lxb.lxbsessential.data.NamedLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

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

    public static ITextComponent listEntry(NamedLocation entry) {
        return new TextComponentString(" - " + entry.name + ": " + entry.location.describe());
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

    // storage / bed

    public static ITextComponent saveFailed() {
        return error("Failed to save, please contact an admin.");
    }

    public static ITextComponent spawnSet() {
        return info("Spawn point set.");
    }
}
