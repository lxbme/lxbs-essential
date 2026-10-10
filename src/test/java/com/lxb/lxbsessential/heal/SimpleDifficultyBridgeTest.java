package com.lxb.lxbsessential.heal;

import com.lxb.lxbsessential.heal.fake.FakeSDCapabilities;
import com.lxb.lxbsessential.heal.fake.FakeTemperature;
import com.lxb.lxbsessential.heal.fake.FakeThirst;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleDifficultyBridgeTest {

    private static final String FAKE = "com.lxb.lxbsessential.heal.fake.";
    private static final String CAPABILITIES = FAKE + "FakeSDCapabilities";
    private static final String TEMPERATURE_ENUM = FAKE + "FakeTemperatureEnum";

    @BeforeEach
    void reset() {
        FakeSDCapabilities.thirst = new FakeThirst();
        FakeSDCapabilities.temperature = new FakeTemperature();
    }

    @Test
    void quenchFillsThirstAndSaturation() throws Exception {
        SimpleDifficultyBridge bridge = SimpleDifficultyBridge.bind(CAPABILITIES, TEMPERATURE_ENUM);
        assertNotNull(bridge);

        assertTrue(bridge.quench(null));
        FakeThirst thirst = FakeSDCapabilities.thirst;
        assertEquals(20, thirst.level);
        assertEquals(20F, thirst.saturation);
        assertEquals(0F, thirst.exhaustion);
        assertEquals(0, thirst.damageCounter);
    }

    /** "Normal" comes from SimpleDifficulty's own NORMAL range rather than a hard-coded number. */
    @Test
    void temperatureGoesToTheMiddleOfNormal() throws Exception {
        SimpleDifficultyBridge bridge = SimpleDifficultyBridge.bind(CAPABILITIES, TEMPERATURE_ENUM);

        assertTrue(bridge.normalizeTemperature(null));
        assertEquals(12, FakeSDCapabilities.temperature.level);
        assertEquals(0, FakeSDCapabilities.temperature.damageCounter);
    }

    @Test
    void temperatureIsSkippedWhenNormalIsUnknown() throws Exception {
        SimpleDifficultyBridge bridge = SimpleDifficultyBridge.bind(CAPABILITIES, FAKE + "NoSuchEnum");

        assertNotNull(bridge);
        assertFalse(bridge.normalizeTemperature(null));
        assertEquals(24, FakeSDCapabilities.temperature.level);
        assertTrue(bridge.quench(null));
    }

    @Test
    void missingSimpleDifficultyIsNotBindable() {
        assertNull(SimpleDifficultyBridge.bind(FAKE + "NoSuchClass", TEMPERATURE_ENUM));
    }
}
