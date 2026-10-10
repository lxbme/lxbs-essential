package com.lxb.lxbsessential.phase;

import com.lxb.lxbsessential.phase.fake.FakeConfig;
import com.lxb.lxbsessential.phase.fake.FakeRegionLookup;
import com.lxb.lxbsessential.phase.fake.FakeSaveData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SrpBridgeTest {

    private static final String FAKE = "com.lxb.lxbsessential.phase.fake.";
    private static final String SAVE_DATA = FAKE + "FakeSaveData";
    private static final String COMMAND = FAKE + "FakeCommand";
    private static final String CONFIG = FAKE + "FakeConfig";
    private static final String REGION = FAKE + "FakeRegionLookup";

    @BeforeEach
    void reset() {
        FakeSaveData.dimension = new FakeSaveData(0, 0);
        FakeRegionLookup.result = FakeSaveData.dimension;
        FakeConfig.useEvolution = true;
    }

    private static SrpBridge bindWithRegions() {
        return SrpBridge.bind(SAVE_DATA, COMMAND, CONFIG, REGION);
    }

    @Test
    void readsRegionDataWhenChunkPhasesApply() throws Exception {
        FakeRegionLookup.result = new FakeSaveData(3, 1200);
        SrpBridge bridge = bindWithRegions();

        SrpBridge.Reading reading = bridge.read(null, null, null, 0);
        assertEquals(3, reading.phase);
        assertEquals(1200, reading.points);
        assertEquals(25000, reading.nextThreshold);
        assertTrue(reading.region);
    }

    /** SRPMixins hands back the dimension-wide data in dimensions excluded from chunk phases. */
    @Test
    void dimensionScopeWhenRegionLookupReturnsDimensionData() throws Exception {
        FakeSaveData.dimension = new FakeSaveData(10, 2000000000);
        FakeRegionLookup.result = FakeSaveData.dimension;

        SrpBridge.Reading reading = bindWithRegions().read(null, null, null, 111);
        assertEquals(10, reading.phase);
        assertFalse(reading.region);
    }

    @Test
    void withoutSrpMixinsReadsDimensionData() throws Exception {
        FakeSaveData.dimension = new FakeSaveData(0, 120);
        SrpBridge bridge = SrpBridge.bind(SAVE_DATA, COMMAND, CONFIG, FAKE + "NoSuchClass");

        assertNotNull(bridge);
        SrpBridge.Reading reading = bridge.read(null, null, null, 0);
        assertEquals(0, reading.phase);
        assertEquals(400, reading.nextThreshold);
        assertFalse(reading.region);
    }

    @Test
    void phaseMinusOneCountsTowardsPhaseZero() throws Exception {
        FakeRegionLookup.result = new FakeSaveData(-1, -40);

        SrpBridge.Reading reading = bindWithRegions().read(null, null, null, 0);
        assertEquals(-1, reading.phase);
        assertEquals(0, reading.nextThreshold);
    }

    @Test
    void lastPhaseHasNoNextThreshold() throws Exception {
        FakeRegionLookup.result = new FakeSaveData(10, 1900000000);

        assertEquals(0, bindWithRegions().read(null, null, null, 0).nextThreshold);
    }

    /** A future SRP with a different API must disable /phase, not crash the server. */
    @Test
    void changedApiIsNotBindable() {
        assertNull(SrpBridge.bind(FAKE + "FakeBrokenSaveData", COMMAND, CONFIG, REGION));
        assertNull(SrpBridge.bind(FAKE + "NoSuchClass", COMMAND, CONFIG, REGION));
    }

    @Test
    void reportsWhetherEvolutionIsEnabled() {
        SrpBridge bridge = bindWithRegions();
        assertTrue(bridge.evolutionEnabled());
        FakeConfig.useEvolution = false;
        assertFalse(bridge.evolutionEnabled());
    }

    @Test
    void failuresInsideSrpSurfaceAsReflectiveErrors() {
        SrpBridge bridge = SrpBridge.bind(FAKE + "FakeThrowingSaveData", COMMAND, CONFIG, FAKE + "NoSuchClass");

        assertNotNull(bridge);
        assertThrows(ReflectiveOperationException.class, () -> bridge.read(null, null, null, 0));
    }
}
