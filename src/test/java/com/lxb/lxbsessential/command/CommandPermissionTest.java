package com.lxb.lxbsessential.command;

import net.minecraft.command.CommandBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandPermissionTest {

    /** Vanilla refuses almost every command for non-op players unless checkPermission is overridden. */
    @Test
    void homeCommandsAreOpenToEveryone() {
        CommandBase[] commands = {new CommandSetHome(), new CommandHome(), new CommandDelHome(), new CommandHomes()};
        for (CommandBase command : commands) {
            assertTrue(command.checkPermission(null, null), command.getName());
            assertEquals(0, command.getRequiredPermissionLevel(), command.getName());
        }
    }

    @Test
    void homeCommandNames() {
        assertEquals("sethome", new CommandSetHome().getName());
        assertEquals("home", new CommandHome().getName());
        assertEquals("delhome", new CommandDelHome().getName());
        assertEquals("homes", new CommandHomes().getName());
    }

    @Test
    void warpAndBackCommandsAreOpenToEveryone() {
        CommandBase[] commands = {new CommandWarp(), new CommandWarps(), new CommandBack()};
        for (CommandBase command : commands) {
            assertTrue(command.checkPermission(null, null), command.getName());
            assertEquals(0, command.getRequiredPermissionLevel(), command.getName());
        }
    }

    @Test
    void warpManagementRequiresOp() {
        assertEquals(2, new CommandSetWarp().getRequiredPermissionLevel());
        assertEquals(2, new CommandDelWarp().getRequiredPermissionLevel());
    }

    @Test
    void warpAndBackCommandNames() {
        assertEquals("warp", new CommandWarp().getName());
        assertEquals("warps", new CommandWarps().getName());
        assertEquals("setwarp", new CommandSetWarp().getName());
        assertEquals("delwarp", new CommandDelWarp().getName());
        assertEquals("back", new CommandBack().getName());
    }

    @Test
    void shareDeathIsOpenToEveryone() {
        CommandShareDeath command = new CommandShareDeath();
        assertEquals("sharedeath", command.getName());
        assertTrue(command.checkPermission(null, null));
        assertEquals(0, command.getRequiredPermissionLevel());
    }

    @Test
    void phaseIsOpenToEveryone() {
        CommandPhase command = new CommandPhase(null);
        assertEquals("phase", command.getName());
        assertTrue(command.checkPermission(null, null));
        assertEquals(0, command.getRequiredPermissionLevel());
    }

    @Test
    void fixRequiresOp() {
        assertEquals("fix", new CommandFix().getName());
        assertEquals(2, new CommandFix().getRequiredPermissionLevel());
    }

    @Test
    void healRequiresOp() {
        assertEquals("heal", new CommandHeal(null, null).getName());
        assertEquals(2, new CommandHeal(null, null).getRequiredPermissionLevel());
    }
}
