package com.lxb.lxbsessential.heal;

import com.lxb.lxbsessential.heal.fake.FakeDamageModel;
import com.lxb.lxbsessential.heal.fake.FakePart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstAidBridgeTest {

    private static final String HOLDER = "com.lxb.lxbsessential.heal.fake.FakeHealthCapabilityHolder";

    /** With First Aid, vanilla health follows the body parts, so every part must be restored. */
    @Test
    void everyBodyPartIsRestoredAndResynced() throws Exception {
        FakePart head = new FakePart(4, 1F);
        FakePart body = new FakePart(6, 0.5F);
        FakePart leg = new FakePart(4, 4F);
        FakeDamageModel model = new FakeDamageModel(head, body, leg);

        FirstAidBridge bridge = FirstAidBridge.bind(HOLDER);
        assertNotNull(bridge);
        bridge.healModel(model, null);

        assertEquals(4F, head.currentHealth);
        assertEquals(6F, body.currentHealth);
        assertEquals(4F, leg.currentHealth);
        assertTrue(model.resynced);
    }

    @Test
    void healingDoesNotApplyFirstAidDebuffs() throws Exception {
        FakePart arm = new FakePart(4, 1F);
        FirstAidBridge.bind(HOLDER).healModel(new FakeDamageModel(arm), null);
        assertFalse(arm.lastApplyDebuff);
    }

    @Test
    void missingFirstAidIsNotBindable() {
        assertNull(FirstAidBridge.bind("com.lxb.lxbsessential.heal.fake.NoSuchClass"));
    }
}
