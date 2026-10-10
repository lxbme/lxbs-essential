package com.lxb.lxbsessential.util;

import com.lxb.lxbsessential.data.Location;
import com.lxb.lxbsessential.data.NamedLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class MessagesTest {

    private static final Location DEATH = new Location(0, -196.5, 63, 293.2, 0F, 0F);

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
    void listEntryRunsTeleportCommandWhenClicked() {
        NamedLocation home = new NamedLocation("Base", DEATH);
        ITextComponent line = Messages.listEntry(home, "/home");

        assertEquals(" - Base: dim 0 (-197, 63, 293)", line.getUnformattedText());
        ClickEvent click = line.getStyle().getClickEvent();
        assertNotNull(click);
        assertEquals(ClickEvent.Action.RUN_COMMAND, click.getAction());
        assertEquals("/home Base", click.getValue());
        HoverEvent hover = line.getStyle().getHoverEvent();
        assertNotNull(hover);
        assertEquals("Click to teleport", hover.getValue().getUnformattedText());
    }

    @Test
    void deathNoticeOffersBackAndShare() {
        ITextComponent notice = Messages.deathNotice(DEATH, true);

        assertEquals("You died at dim 0 (-197, 63, 293). [Back] [Share]", notice.getUnformattedText());
        assertEquals("/back", part(notice, "[Back]").getStyle().getClickEvent().getValue());
        assertEquals("/sharedeath", part(notice, "[Share]").getStyle().getClickEvent().getValue());
        assertEquals(ClickEvent.Action.RUN_COMMAND, part(notice, "[Share]").getStyle().getClickEvent().getAction());
    }

    @Test
    void deathNoticeWithoutBackWhenBackOnDeathIsOff() {
        ITextComponent notice = Messages.deathNotice(DEATH, false);

        assertEquals("You died at dim 0 (-197, 63, 293). [Share]", notice.getUnformattedText());
        assertNull(part(notice, "[Back]"));
    }

    @Test
    void sharedDeathNamesThePlayer() {
        assertEquals("LxbThh died at dim 0 (-197, 63, 293).",
                Messages.deathShared("LxbThh", DEATH).getUnformattedText());
    }

    /** 1.12 has no copy-to-clipboard click action, so the coordinates go into the chat box instead. */
    @Test
    void deathNoticeCoordinatesFillTheChatBox() {
        ITextComponent coordinates = part(Messages.deathNotice(DEATH, true), "dim 0 (-197, 63, 293)");

        assertNotNull(coordinates);
        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, coordinates.getStyle().getClickEvent().getAction());
        assertEquals("-197 63 293", coordinates.getStyle().getClickEvent().getValue());
        assertEquals("-197 63 293", coordinates.getStyle().getInsertion());
    }

    @Test
    void sharedDeathCoordinatesFillTheChatBox() {
        ITextComponent coordinates = part(Messages.deathShared("LxbThh", DEATH), "dim 0 (-197, 63, 293)");

        assertNotNull(coordinates);
        assertEquals(ClickEvent.Action.SUGGEST_COMMAND, coordinates.getStyle().getClickEvent().getAction());
        assertEquals("-197 63 293", coordinates.getStyle().getClickEvent().getValue());
        assertEquals("-197 63 293", coordinates.getStyle().getInsertion());
    }

    @Test
    void phaseWithProgressInRegion() {
        assertEquals("Parasite phase here: 3 (this region) - 1200 / 25000 points to phase 4 (4%)",
                Messages.phaseReport(3, 1200, 25000, true).getUnformattedText());
    }

    @Test
    void phaseInDimensionScope() {
        assertEquals("Parasite phase here: 0 (this dimension) - 120 / 400 points to phase 1 (30%)",
                Messages.phaseReport(0, 120, 400, false).getUnformattedText());
    }

    @Test
    void phaseProgressNeverShowsMoreThanHundredPercent() {
        assertEquals("Parasite phase here: 0 (this region) - 450 / 400 points to phase 1 (100%)",
                Messages.phaseReport(0, 450, 400, true).getUnformattedText());
    }

    @Test
    void phaseNotStartedHasNoPercentage() {
        assertEquals("Parasite phase here: -1 (this region) - -40 / 0 points to phase 0",
                Messages.phaseReport(-1, -40, 0, true).getUnformattedText());
    }

    @Test
    void phaseAtMaximum() {
        assertEquals("Parasite phase here: 10 (this dimension) - maximum phase",
                Messages.phaseReport(10, 1900000000, 0, false).getUnformattedText());
    }

    @Test
    void phaseMinusTwoCannotEvolve() {
        assertEquals("Parasites cannot evolve here.",
                Messages.phaseReport(-2, -20000, 0, false).getUnformattedText());
    }
}
