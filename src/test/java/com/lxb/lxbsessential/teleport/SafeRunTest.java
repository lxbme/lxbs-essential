package com.lxb.lxbsessential.teleport;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SafeRunTest {

    @Test
    void successfulTaskRunsAndReportsNothing() {
        List<String> log = new ArrayList<>();
        assertTrue(SafeRun.run(() -> log.add("ran"), e -> log.add("error")));
        assertEquals(1, log.size());
        assertEquals("ran", log.get(0));
    }

    /** A teleport that throws inside the server tick must not crash the whole server. */
    @Test
    void exceptionIsHandedToHandlerInsteadOfPropagating() {
        IllegalStateException boom = new IllegalStateException("boom");
        List<RuntimeException> errors = new ArrayList<>();
        assertFalse(SafeRun.run(() -> {
            throw boom;
        }, errors::add));
        assertEquals(1, errors.size());
        assertSame(boom, errors.get(0));
    }
}
