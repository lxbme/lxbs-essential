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
}
