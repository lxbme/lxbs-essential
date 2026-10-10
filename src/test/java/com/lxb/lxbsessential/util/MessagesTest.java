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

    private static String text(java.util.List<ITextComponent> lines) {
        StringBuilder sb = new StringBuilder();
        for (ITextComponent line : lines) {
            sb.append(line.getUnformattedText()).append('\n');
        }
        return sb.toString();
    }

    @Test
    void bankHelpListsPlayerCommandsRulesAndNoAdmin() {
        java.util.List<ITextComponent> lines = Messages.bankHelp(1.0, 30, 30970, 100, 5.0);
        String all = text(lines);

        assertEquals(true, all.contains("/xpbank deposit <levels|all>"));
        assertEquals(true, all.contains("/xpbank withdraw <levels|all>"));
        assertEquals(true, all.contains("/xpbank pay <player> <points>"));
        assertEquals(false, all.contains("admin"));
        assertEquals("Interest: 1% every 30 minutes you are online, on balances up to 30970 XP (level 100).",
                lines.get(lines.size() - 2).getUnformattedText());
        assertEquals("Withdrawals cost a 5% fee.", lines.get(lines.size() - 1).getUnformattedText());
    }

    @Test
    void bankHelpFormatsFractionsSingularsAndFreeWithdrawals() {
        java.util.List<ITextComponent> lines = Messages.bankHelp(1.5, 1, 500, 19, 0.0);
        assertEquals("Interest: 1.5% every 1 minute you are online, on balances up to 500 XP (level 19).",
                lines.get(lines.size() - 2).getUnformattedText());
        assertEquals("Withdrawals are free.", lines.get(lines.size() - 1).getUnformattedText());
    }

    @Test
    void bankHelpWithoutInterest() {
        java.util.List<ITextComponent> lines = Messages.bankHelp(0.0, 30, 30970, 100, 5.0);
        assertEquals("Interest: none.", lines.get(lines.size() - 2).getUnformattedText());
    }

    @Test
    void bankBalanceShowsLevelsAndNextInterest() {
        assertEquals("Balance: 5345 XP (= level 50 from zero). Next interest in 12 minutes.",
                Messages.bankBalance(5345, 50, 12).getUnformattedText());
        assertEquals("Balance: 0 XP (= level 0 from zero).",
                Messages.bankBalance(0, 0, -1).getUnformattedText());
    }

    @Test
    void withdrawalMentionsTheFeeOnlyWhenCharged() {
        assertEquals("Withdrew 345 XP (fee 18 XP). Balance: 999 XP.",
                Messages.withdrew(345, 18, 999).getUnformattedText());
        assertEquals("Withdrew 345 XP. Balance: 999 XP.",
                Messages.withdrew(345, 0, 999).getUnformattedText());
    }

    @Test
    void interestCreditedText() {
        assertEquals("+12 XP interest (balance: 1234)", Messages.interestCredited(12, 1234).getUnformattedText());
    }
}
