package com.lxb.lxbsessential.util;

import com.lxb.lxbsessential.info.ArmorStats;
import com.lxb.lxbsessential.info.ItemReport;
import com.lxb.lxbsessential.info.MeleeStats;
import com.lxb.lxbsessential.info.RangedStats;
import com.lxb.lxbsessential.info.ToolStats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Chat lines for /info. Like {@link Messages} everything is literal English, except enchantment names:
 * those are translation components, because every client in the modpack has the keys, and the client
 * then shows them in the player's own language.
 */
public final class ItemInfoText {

    /** Larger item hovers risk a chat packet big enough to kick the player. */
    static final int MAX_HOVER_LENGTH = 16000;

    private static final String ESTIMATE = " (vanilla estimate)";
    private static final String[] ROMAN = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};

    private ItemInfoText() {
    }

    public static ITextComponent notHolding() {
        return Messages.error("You are not holding anything.");
    }

    public static ITextComponent nothingToShare() {
        return Messages.error("Nothing to share, run /info again.");
    }

    public static List<ITextComponent> report(ItemReport report, boolean shared) {
        List<ITextComponent> lines = new ArrayList<>();
        lines.add(header(report, shared));
        lines.add(plain(" ID: " + id(report.stack) + " (" + report.modName + ")"));
        String durability = durability(report.stack);
        if (durability != null) {
            lines.add(plain(" Durability: " + durability));
        }
        ITextComponent enchantments = enchantments(report.stack);
        if (enchantments != null) {
            lines.add(enchantments);
        }
        if (report.melee != null) {
            melee(lines, report.melee, shared ? report.holderName : "you");
        }
        if (report.ranged != null) {
            ranged(lines, report.ranged);
        }
        if (report.tools != null) {
            tools(lines, report.tools);
        }
        if (report.shield) {
            lines.add(plain(" Shield"));
        }
        if (report.armor != null) {
            armor(lines, report.armor);
        }
        return lines;
    }

    private static ITextComponent header(ItemReport report, boolean shared) {
        ITextComponent header = Messages.colored(
                shared ? report.holderName + " shared: " : "── Item Info ── ", TextFormatting.GOLD);
        header.appendSibling(itemName(report.stack));
        if (!shared) {
            header.appendSibling(new TextComponentString(" "));
            header.appendSibling(Messages.button("[Share]", "/info share", "Show this item to everyone"));
        }
        return header;
    }

    /** Vanilla's [name] component: rarity colour and a hover the client renders as the full tooltip. */
    private static ITextComponent itemName(ItemStack stack) {
        ITextComponent name = stack.getTextComponent();
        if (stack.writeToNBT(new NBTTagCompound()).toString().length() > MAX_HOVER_LENGTH) {
            name.getStyle().setHoverEvent(null);
        }
        return name;
    }

    private static String id(ItemStack stack) {
        ResourceLocation name = stack.getItem().getRegistryName();
        String id = name == null ? "unknown" : name.toString();
        return stack.getHasSubtypes() ? id + ":" + stack.getMetadata() : id;
    }

    @Nullable
    private static String durability(ItemStack stack) {
        if (stack.getMaxDamage() <= 0) {
            return null;
        }
        if (!stack.isItemStackDamageable()) {
            return "Unbreakable";
        }
        int max = stack.getMaxDamage();
        return (max - stack.getItemDamage()) + "/" + max;
    }

    @Nullable
    private static ITextComponent enchantments(ItemStack stack) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        if (enchantments.isEmpty()) {
            return null;
        }
        ITextComponent line = plain(" Enchantments: ");
        boolean first = true;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            if (!first) {
                line.appendSibling(new TextComponentString(", "));
            }
            first = false;
            line.appendSibling(enchantment(entry.getKey(), entry.getValue()));
        }
        return line;
    }

    /** Same shape as Enchantment.getTranslatedName, but translated on the client. */
    private static ITextComponent enchantment(Enchantment enchantment, int level) {
        ITextComponent name = new TextComponentTranslation(enchantment.getName());
        if (level != 1 || enchantment.getMaxLevel() != 1) {
            name.appendSibling(new TextComponentString(" "));
            name.appendSibling(new TextComponentTranslation("enchantment.level." + level));
        }
        if (enchantment.isCurse()) {
            name.getStyle().setColor(TextFormatting.RED);
        }
        return name;
    }

    private static void melee(List<ITextComponent> lines, MeleeStats melee, String holder) {
        lines.add(plain(" Melee (item): " + meleeLine(melee.item)));
        lines.add(plain(" Melee (" + holder + "): " + meleeLine(melee.holder)));
        lines.add(note("  Excludes on-hit effects from mods (RLCombat, weapon traits, etc.)"));
    }

    private static String meleeLine(MeleeStats.Line line) {
        StringBuilder text = new StringBuilder()
                .append(n(line.total())).append(" damage, crit ").append(n(line.crit()))
                .append(", ").append(n(line.speed)).append(" attacks/s, DPS ").append(n(line.dps()));
        if (line.undeadDiffers()) {
            text.append(", vs undead ").append(n(line.vsUndead()));
        }
        if (line.arthropodsDiffer()) {
            text.append(", vs arthropods ").append(n(line.vsArthropods()));
        }
        return text.toString();
    }

    private static void ranged(List<ITextComponent> lines, RangedStats ranged) {
        if (ranged.hasDamage) {
            lines.add(plain(" Arrow damage: " + ranged.minHit + "-" + ranged.maxHit
                    + " (normal arrow, full draw " + n(ranged.drawSeconds) + "s)"
                    + (ranged.estimate ? ESTIMATE : "")));
        }
        if (ranged.power > 0) {
            // vanilla adds 0.5 * level + 0.5 to the arrow's base damage of 2
            lines.add(plain(" Power " + roman(ranged.power) + ": +" + 25 * (ranged.power + 1) + "% arrow damage"));
        }
        if (ranged.punch > 0) {
            lines.add(plain(" Punch " + roman(ranged.punch) + ": extra knockback"));
        }
        if (ranged.flame) {
            lines.add(plain(" Flame: sets targets on fire"));
        }
        if (ranged.infinity) {
            lines.add(plain(" Infinity: normal arrows are not used up"));
        }
        lines.add(note("  Arrow type and mods can change this."));
    }

    private static void tools(List<ITextComponent> lines, ToolStats tools) {
        for (ToolStats.Entry entry : tools.entries) {
            List<String> parts = new ArrayList<>();
            if (entry.harvestLevel >= 0) {
                parts.add("harvest level " + entry.harvestLevel);
            }
            if (entry.referenceBlock != null) {
                parts.add("speed " + n(entry.digSpeed) + " on " + entry.referenceBlock);
            }
            lines.add(plain(" " + capitalize(entry.toolClass) + ": " + String.join(", ", parts)));
        }
    }

    private static void armor(List<ITextComponent> lines, ArmorStats armor) {
        lines.add(plain(" Armor (" + armor.slot.getName() + "): " + n(armor.armor) + " armor, "
                + n(armor.toughness) + " toughness" + (armor.estimate ? ESTIMATE : "")));
        String protection = protection(armor.epf);
        if (protection != null) {
            lines.add(plain(" Protection: " + protection));
        }
        boolean enchanted = armor.epf.containsKey(ArmorStats.Protection.GENERIC);
        String where = armor.firstAid ? " damage on " + bodyPart(armor.slot) : " damage, this piece alone";
        for (ArmorStats.Example example : armor.examples) {
            String text = "  " + n(example.damage) + where + ": " + n(example.afterArmor) + " after armor";
            if (enchanted) {
                text += ", " + n(example.afterEnchantments) + " after enchantments";
            }
            lines.add(plain(text));
        }
    }

    /** Protection counts against every type, so other types are listed only where they differ from normal. */
    @Nullable
    private static String protection(Map<ArmorStats.Protection, Integer> epf) {
        Integer normal = epf.get(ArmorStats.Protection.GENERIC);
        List<String> parts = new ArrayList<>();
        for (Map.Entry<ArmorStats.Protection, Integer> entry : epf.entrySet()) {
            if (entry.getKey() != ArmorStats.Protection.GENERIC && entry.getValue().equals(normal)) {
                continue;
            }
            // vanilla caps the protection factor at 20, 4% each
            parts.add(entry.getKey().label + " " + entry.getValue() + " (" + Math.min(entry.getValue(), 20) * 4 + "%)");
        }
        return parts.isEmpty() ? null : String.join(", ", parts);
    }

    /** First Aid's chest piece covers the body and both arms. */
    private static String bodyPart(EntityEquipmentSlot slot) {
        switch (slot) {
            case HEAD:
                return "head";
            case CHEST:
                return "body and arms";
            case LEGS:
                return "legs";
            case FEET:
                return "feet";
            default:
                return slot.getName();
        }
    }

    static String roman(int level) {
        return level >= 1 && level <= ROMAN.length ? ROMAN[level - 1] : Integer.toString(level);
    }

    private static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static String n(double value) {
        return Messages.number(value);
    }

    private static ITextComponent plain(String text) {
        return new TextComponentString(text);
    }

    private static ITextComponent note(String text) {
        return Messages.colored(text, TextFormatting.GRAY);
    }
}
