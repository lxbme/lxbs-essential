package com.lxb.lxbsessential.data;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerDataTest {

    private static final Location L1 = new Location(0, 1.5, 64, -2.5, 90F, 10F);
    private static final Location L2 = new Location(-1, 10, 70, 10, 0F, 0F);

    @Test
    void createThenGetIsCaseInsensitive() {
        PlayerData data = new PlayerData();
        assertEquals(PlayerData.SetResult.CREATED, data.setHome("Base", L1, 3));
        assertEquals(L1, data.getHome("base").location);
        assertEquals("Base", data.getHome("BASE").name);
    }

    @Test
    void overwriteKeepsCountAndUpdatesSpelling() {
        PlayerData data = new PlayerData();
        data.setHome("base", L1, 3);
        assertEquals(PlayerData.SetResult.OVERWRITTEN, data.setHome("BASE", L2, 3));
        assertEquals(1, data.homeCount());
        assertEquals("BASE", data.getHome("base").name);
        assertEquals(L2, data.getHome("base").location);
    }

    @Test
    void limitBlocksNewNames() {
        PlayerData data = new PlayerData();
        data.setHome("a", L1, 2);
        data.setHome("b", L1, 2);
        assertEquals(PlayerData.SetResult.LIMIT_REACHED, data.setHome("c", L1, 2));
        assertNull(data.getHome("c"));
        assertEquals(2, data.homeCount());
    }

    @Test
    void overwriteAllowedAtLimit() {
        PlayerData data = new PlayerData();
        data.setHome("Base", L1, 1);
        assertEquals(PlayerData.SetResult.OVERWRITTEN, data.setHome("base", L2, 1));
        assertEquals(L2, data.getHome("BASE").location);
    }

    @Test
    void deleteHome() {
        PlayerData data = new PlayerData();
        data.setHome("Base", L1, 3);
        assertTrue(data.deleteHome("BASE"));
        assertFalse(data.deleteHome("base"));
        assertEquals(0, data.homeCount());
    }

    @Test
    void listHomesSortedCaseInsensitive() {
        PlayerData data = new PlayerData();
        data.setHome("b", L1, 5);
        data.setHome("A", L1, 5);
        data.setHome("c", L1, 5);
        List<NamedLocation> homes = data.listHomes();
        assertEquals("A", homes.get(0).name);
        assertEquals("b", homes.get(1).name);
        assertEquals("c", homes.get(2).name);
    }

    @Test
    void backDefaultsToNullAndCanBeSet() {
        PlayerData data = new PlayerData();
        assertNull(data.getBack());
        data.setBack(L2);
        assertEquals(L2, data.getBack());
    }

    @Test
    void normalizeRekeysAndDropsBrokenEntries() {
        String json = "{\"homes\":{"
                + "\"WRONGKEY\":{\"name\":\"Base\",\"location\":{\"dim\":0,\"x\":1,\"y\":2,\"z\":3,\"yaw\":0,\"pitch\":0}},"
                + "\"nullEntry\":null,"
                + "\"badName\":{\"name\":\"bad name\",\"location\":{\"dim\":0,\"x\":1,\"y\":2,\"z\":3,\"yaw\":0,\"pitch\":0}},"
                + "\"noLocation\":{\"name\":\"ok\"}"
                + "}}";
        PlayerData data = new Gson().fromJson(json, PlayerData.class);
        data.normalize();
        assertEquals(1, data.homeCount());
        assertNotNull(data.getHome("base"));
        assertEquals(new Location(0, 1, 2, 3, 0F, 0F), data.getHome("base").location);
    }

    @Test
    void emptyJsonObjectIsUsable() {
        PlayerData data = new Gson().fromJson("{}", PlayerData.class);
        data.normalize();
        assertEquals(0, data.homeCount());
        assertNull(data.getBack());
        assertEquals(PlayerData.SetResult.CREATED, data.setHome("home", L1, 3));
    }
}
