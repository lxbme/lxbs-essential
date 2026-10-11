package com.lxb.lxbsessential.util;

import com.lxb.lxbsessential.info.FirstAidArmor;
import com.lxb.lxbsessential.info.Integrations;
import com.lxb.lxbsessential.info.ItemReport;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ItemInfoTextTest {

    private static final String UTILS = "com.lxb.lxbsessential.info.fake.FakeArmorUtils";
    private static final String CONFIG = "com.lxb.lxbsessential.info.fake.FakeFirstAidConfig";

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    private static ItemReport report(ItemStack stack, double holderDamage, Integrations integrations) {
        return ItemReport.build(stack, "Steve", "Minecraft", holderDamage, 1.6, integrations);
    }

    private static ItemReport report(ItemStack stack) {
        return report(stack, 1, Integrations.NONE);
    }

    private static List<String> texts(List<ITextComponent> lines) {
        List<String> texts = new ArrayList<>();
        for (ITextComponent line : lines) {
            texts.add(line.getUnformattedText());
        }
        return texts;
    }

    /** Iterating a component yields copies of every part with the inherited style applied. */
    private static ITextComponent part(ITextComponent message, String text) {
        for (ITextComponent part : message) {
            if (part.getUnformattedComponentText().equals(text)) {
                return part;
            }
        }
        return null;
    }

    /** Walks siblings directly: iterating a component skips translation components themselves. */
    private static boolean hasTranslation(ITextComponent component, String key) {
        if (component instanceof TextComponentTranslation && ((TextComponentTranslation) component).getKey().equals(key)) {
            return true;
        }
        for (ITextComponent sibling : component.getSiblings()) {
            if (hasTranslation(sibling, key)) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack blade() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setStackDisplayName("Blade");
        sword.addEnchantment(Enchantments.SHARPNESS, 5);
        return sword;
    }

    @Test
    void privateSwordReport() {
        List<ITextComponent> lines = ItemInfoText.report(report(blade(), 13, Integrations.NONE), false);
        List<String> texts = texts(lines);

        assertEquals("── Item Info ── [Blade] [Share]", texts.get(0));
        ClickEvent share = part(lines.get(0), "[Share]").getStyle().getClickEvent();
        assertEquals(ClickEvent.Action.RUN_COMMAND, share.getAction());
        assertEquals("/info share", share.getValue());
        assertEquals(" ID: minecraft:diamond_sword (Minecraft)", texts.get(1));
        assertEquals(" Durability: 1561/1561", texts.get(2));
        assertTrue(texts.get(3).startsWith(" Enchantments: "));
        assertTrue(hasTranslation(lines.get(3), "enchantment.damage.all"));
        assertTrue(hasTranslation(lines.get(3), "enchantment.level.5"));
        assertEquals(Arrays.asList(
                " Melee (item): 10 damage, crit 13.5, 1.6 attacks/s, DPS 16",
                " Melee (you): 16 damage, crit 22.5, 1.6 attacks/s, DPS 25.6",
                "  Excludes on-hit effects from mods (RLCombat, weapon traits, etc.)"),
                texts.subList(4, 7));
        assertEquals(7, texts.size());
    }

    @Test
    void sharedReportNamesTheHolderAndHasNoShareButton() {
        List<ITextComponent> lines = ItemInfoText.report(report(blade(), 13, Integrations.NONE), true);
        List<String> texts = texts(lines);

        assertEquals("Steve shared: [Blade]", texts.get(0));
        assertNull(part(lines.get(0), "[Share]"));
        assertTrue(texts.contains(" Melee (Steve): 16 damage, crit 22.5, 1.6 attacks/s, DPS 25.6"));
    }

    @Test
    void itemNameShowsTheItemOnHover() {
        ITextComponent header = ItemInfoText.report(report(blade()), false).get(0);
        HoverEvent hover = part(header, "Blade").getStyle().getHoverEvent();

        assertNotNull(hover);
        assertEquals(HoverEvent.Action.SHOW_ITEM, hover.getAction());
    }

    /** A chat packet that is too large kicks the player, so huge items get no hover. */
    @Test
    void oversizedItemHasNoHover() {
        ItemStack stack = blade();
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < ItemInfoText.MAX_CHAT_JSON_BYTES + 1000; i++) {
            padding.append('x');
        }
        stack.getTagCompound().setString("pad", padding.toString());

        ITextComponent header = ItemInfoText.report(report(stack), false).get(0);
        assertNull(part(header, "Blade").getStyle().getHoverEvent());
    }

    /**
     * The chat packet limit is 32767 UTF-8 bytes of JSON, not characters: a long Chinese book is short in
     * characters but three bytes per character, and the escaped hover is longer still. Too big kicks the
     * viewer, and with [Share] everyone online.
     */
    @Test
    void chineseTextCountsInBytesForTheHoverLimit() {
        ItemStack stack = blade();
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < 11000; i++) {
            padding.append('中');
        }
        stack.getTagCompound().setString("pad", padding.toString());

        for (boolean shared : new boolean[]{false, true}) {
            ITextComponent header = ItemInfoText.report(report(stack), shared).get(0);
            assertNull(part(header, "Blade").getStyle().getHoverEvent());
            assertTrue(ITextComponent.Serializer.componentToJson(header)
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= ItemInfoText.MAX_CHAT_JSON_BYTES);
        }
    }

    @Test
    void smiteAddsTheUndeadDamage() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.addEnchantment(Enchantments.SMITE, 5);

        assertTrue(texts(ItemInfoText.report(report(sword, 7, Integrations.NONE), false))
                .contains(" Melee (item): 7 damage, crit 10.5, 1.6 attacks/s, DPS 11.2, vs undead 19.5"));
    }

    @Test
    void bowReport() {
        ItemStack bow = new ItemStack(Items.BOW);
        bow.addEnchantment(Enchantments.POWER, 5);
        bow.addEnchantment(Enchantments.PUNCH, 2);
        bow.addEnchantment(Enchantments.FLAME, 1);
        bow.addEnchantment(Enchantments.INFINITY, 1);
        List<String> texts = texts(ItemInfoText.report(report(bow), false));

        assertTrue(texts.containsAll(Arrays.asList(
                " Arrow damage: 15-23 (normal arrow, full draw 1s)",
                " Power V: +150% arrow damage",
                " Punch II: extra knockback",
                " Flame: sets targets on fire",
                " Infinity: normal arrows are not used up",
                "  Arrow type and mods can change this.")), texts.toString());
        assertFalse(texts.stream().anyMatch(t -> t.startsWith(" Melee")));
    }

    @Test
    void pickaxeReportsHarvestLevelAndSpeed() {
        assertTrue(texts(ItemInfoText.report(report(new ItemStack(Items.DIAMOND_PICKAXE)), false))
                .contains(" Pickaxe: harvest level 3, speed 8 on stone"));
    }

    @Test
    void shieldIsLabelled() {
        List<String> texts = texts(ItemInfoText.report(report(new ItemStack(Items.SHIELD)), false));

        assertTrue(texts.contains(" Shield"));
        assertTrue(texts.contains(" Durability: 336/336"));
    }

    @Test
    void vanillaArmorReport() {
        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 4);
        List<String> texts = texts(ItemInfoText.report(report(chestplate), false));

        assertTrue(texts.containsAll(Arrays.asList(
                " Armor (chest): 8 armor, 2 toughness",
                " Protection: normal 4 (16%)",
                "  10 damage, this piece alone: 8.4 after armor, 7.06 after enchantments",
                "  30 damage, this piece alone: 28.08 after armor, 23.59 after enchantments")), texts.toString());
    }

    @Test
    void protectionListsOtherTypesOnlyWhereTheyDiffer() {
        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 2);
        chestplate.addEnchantment(Enchantments.FIRE_PROTECTION, 3);
        List<String> texts = texts(ItemInfoText.report(report(chestplate), false));

        assertTrue(texts.contains(" Protection: normal 2 (8%), fire 8 (32%)"), texts.toString());
    }

    @Test
    void firstAidArmorReportNamesTheBodyPart() {
        ItemStack chestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
        chestplate.addEnchantment(Enchantments.PROTECTION, 4);
        Integrations firstAid = new Integrations(null, FirstAidArmor.bind(UTILS, CONFIG), true);
        List<String> texts = texts(ItemInfoText.report(report(chestplate, 1, firstAid), false));

        assertTrue(texts.containsAll(Arrays.asList(
                " Armor (chest): 18 armor, 4 toughness",
                " Protection: normal 12 (48%)",
                "  10 damage on body and arms: 4.13 after armor, 2.15 after enchantments")), texts.toString());
    }

    @Test
    void armorEstimateIsMarked() {
        Integrations brokenFirstAid = new Integrations(null, null, true);
        List<String> texts = texts(ItemInfoText.report(
                report(new ItemStack(Items.DIAMOND_CHESTPLATE), 1, brokenFirstAid), false));

        assertTrue(texts.contains(" Armor (chest): 8 armor, 2 toughness (vanilla estimate)"), texts.toString());
    }

    @Test
    void unbreakableItem() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setBoolean("Unbreakable", true);
        sword.setTagCompound(tag);

        assertTrue(texts(ItemInfoText.report(report(sword), false)).contains(" Durability: Unbreakable"));
    }

    /** Enchanted books keep their enchantments in StoredEnchantments; single-level ones show no numeral. */
    @Test
    void enchantedBookListsStoredEnchantments() {
        ItemStack book = new ItemStack(Items.ENCHANTED_BOOK);
        ItemEnchantedBook.addEnchantment(book, new EnchantmentData(Enchantments.MENDING, 1));
        List<ITextComponent> lines = ItemInfoText.report(report(book), false);

        ITextComponent enchantments = null;
        for (ITextComponent line : lines) {
            if (line.getUnformattedText().startsWith(" Enchantments: ")) {
                enchantments = line;
            }
        }
        assertNotNull(enchantments);
        Enchantment mending = Enchantments.MENDING;
        assertTrue(hasTranslation(enchantments, mending.getName()));
        assertFalse(hasTranslation(enchantments, "enchantment.level.1"));
    }

    @Test
    void plainItemShowsOnlyHeaderAndId() {
        List<String> texts = texts(ItemInfoText.report(report(new ItemStack(Items.STICK)), false));

        assertEquals(2, texts.size());
        assertEquals(" ID: minecraft:stick (Minecraft)", texts.get(1));
    }

    @Test
    void errorMessages() {
        assertEquals("You are not holding anything.", ItemInfoText.notHolding().getUnformattedText());
        assertEquals("Nothing to share, run /info again.", ItemInfoText.nothingToShare().getUnformattedText());
    }
}
