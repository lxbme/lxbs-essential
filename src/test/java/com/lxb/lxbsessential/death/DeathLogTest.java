package com.lxb.lxbsessential.death;

import com.lxb.lxbsessential.data.Location;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathLogTest {

    private static final UUID A = UUID.fromString("00000000-0000-0000-0000-00000000000a");
    private static final UUID B = UUID.fromString("00000000-0000-0000-0000-00000000000b");
    private static final Location FIRST = new Location(0, -196.5, 63, 293.2, 0F, 0F);
    private static final Location SECOND = new Location(-1, 10, 70, 10, 0F, 0F);

    @Test
    void noDeathMeansNothingToReport() {
        DeathLog log = new DeathLog();
        assertFalse(log.hasDeath(A));
        assertNull(log.takeNotice(A));
        assertNull(log.share(A));
    }

    /** The notice is sent once, on the first respawn after the death. */
    @Test
    void noticeIsHandedOutOnlyOnce() {
        DeathLog log = new DeathLog();
        log.record(A, FIRST);
        assertEquals(FIRST, log.takeNotice(A));
        assertNull(log.takeNotice(A));
        assertTrue(log.hasDeath(A));
    }

    /** One broadcast per death keeps [Share] from being used to spam chat. */
    @Test
    void eachDeathCanBeSharedOnce() {
        DeathLog log = new DeathLog();
        log.record(A, FIRST);
        assertEquals(FIRST, log.share(A));
        assertNull(log.share(A));
        assertTrue(log.hasDeath(A));
    }

    @Test
    void newDeathReplacesOldAndCanBeSharedAgain() {
        DeathLog log = new DeathLog();
        log.record(A, FIRST);
        log.share(A);
        log.record(A, SECOND);
        assertEquals(SECOND, log.takeNotice(A));
        assertEquals(SECOND, log.share(A));
    }

    @Test
    void playersAreIndependent() {
        DeathLog log = new DeathLog();
        log.record(A, FIRST);
        assertFalse(log.hasDeath(B));
        assertNull(log.share(B));
    }

    @Test
    void forgetAndClearDropRecords() {
        DeathLog log = new DeathLog();
        log.record(A, FIRST);
        log.record(B, SECOND);
        log.forget(A);
        assertFalse(log.hasDeath(A));
        assertTrue(log.hasDeath(B));
        log.clear();
        assertFalse(log.hasDeath(B));
    }
}
