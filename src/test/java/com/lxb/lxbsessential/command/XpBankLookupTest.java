package com.lxb.lxbsessential.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XpBankLookupTest {

    /**
     * Only names already in the server's profile cache count; an unknown name must not reach
     * getGameProfileForUsername, which would invent an offline profile or call Mojang on the server thread.
     */
    @Test
    void onlyCachedNamesAreFound() {
        String[] cached = {"lxbthh", "bob"};
        assertEquals("lxbthh", CommandXpBank.cachedName(cached, "LxbThh"));
        assertNull(CommandXpBank.cachedName(cached, "Nobody"));
        assertNull(CommandXpBank.cachedName(new String[0], "bob"));
    }
}
