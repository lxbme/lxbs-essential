package com.lxb.lxbsessential.util;

import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedItemMessagesTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        Bootstrap.register();
    }

    private static ItemStack blade() {
        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
        sword.setStackDisplayName("Blade");
        return sword;
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

    @Test
    void listEntrySuggestsTheGiveCommand() {
        ITextComponent line = Messages.savedItemEntry("Starter", blade(), "LxbThh");

        assertEquals(" - Starter: [Blade] ×1 (saved by LxbThh)", line.getUnformattedText());
        ClickEvent click = line.getStyle().getClickEvent();
        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, click.getAction());
        assertEquals("/si give Starter ", click.getValue());
        assertEquals(HoverEvent.Action.SHOW_ITEM, part(line, "Blade").getStyle().getHoverEvent().getAction());
    }

    @Test
    void entryWhoseItemIsGoneIsMarkedAndNotClickable() {
        ITextComponent line = Messages.savedItemEntry("Old", ItemStack.EMPTY, null);

        assertEquals(" - Old: [missing item] (saved by unknown)", line.getUnformattedText());
        assertNull(line.getStyle().getClickEvent());
    }

    @Test
    void saveAndGiveConfirmations() {
        ItemStack blade = blade();

        assertEquals("Saved [Blade] ×1 as 'Starter'.", Messages.savedItemSaved("Starter", blade).getUnformattedText());
        assertEquals("Updated 'Starter' to [Blade] ×1.", Messages.savedItemUpdated("Starter", blade).getUnformattedText());
        assertEquals("Gave 3 × [Blade] to Steve.", Messages.gave(3, blade, "Steve").getUnformattedText());
        assertEquals("You received 3 × [Blade].", Messages.received(3, blade).getUnformattedText());
        assertEquals(HoverEvent.Action.SHOW_ITEM,
                part(Messages.gave(3, blade, "Steve"), "Blade").getStyle().getHoverEvent().getAction());
    }

    @Test
    void hugeItemIsNamedWithoutItsHover() {
        ItemStack book = blade();
        StringBuilder padding = new StringBuilder();
        for (int i = 0; i < 11000; i++) {
            padding.append('中');
        }
        book.getTagCompound().setString("pad", padding.toString());

        ITextComponent gave = Messages.gave(1, book, "Steve");
        assertNull(part(gave, "Blade").getStyle().getHoverEvent());
        assertTrue(ChatLimits.fits(gave));

        ITextComponent entry = Messages.savedItemEntry("Book", book, "LxbThh");
        HoverEvent hover = part(entry, "Blade").getStyle().getHoverEvent();
        assertNotEquals(HoverEvent.Action.SHOW_ITEM, hover == null ? null : hover.getAction());
        assertTrue(ChatLimits.fits(entry));
    }

    @Test
    void errorTexts() {
        assertEquals("A saved item named 'Starter' already exists. Use /si update Starter to replace it.",
                Messages.savedItemExists("Starter").getUnformattedText());
        assertEquals("No saved item named 'x'.", Messages.noSavedItem("x").getUnformattedText());
        assertEquals("Saved item 'x' no longer exists in this game (was it from a removed mod?)",
                Messages.savedItemMissing("x").getUnformattedText());
        assertEquals("Saved item 'x' has unreadable data in saved_items.json.",
                Messages.savedItemUnreadable("x").getUnformattedText());
        assertEquals("This item cannot be saved exactly, so it was not saved.",
                Messages.savedItemNotExact().getUnformattedText());
        assertEquals("At most 36 at once.", Messages.tooMany(36).getUnformattedText());
        assertEquals("No saved items yet.", Messages.noSavedItems().getUnformattedText());
        assertEquals("Saved items (3):", Messages.savedItemsHeader(3).getUnformattedText());
        assertEquals("Deleted saved item 'x'.", Messages.savedItemDeleted("x").getUnformattedText());
    }
}
