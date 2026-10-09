package com.lxb.lxbsessential.bed;

import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BedSpawnHandlerTest {

    /** Protection mods must get to cancel the click before the spawn point is set. */
    @Test
    void runsAfterEveryOtherHandler() throws Exception {
        Method handler = BedSpawnHandler.class.getMethod("onRightClickBlock", PlayerInteractEvent.RightClickBlock.class);
        SubscribeEvent subscription = handler.getAnnotation(SubscribeEvent.class);
        assertEquals(EventPriority.LOWEST, subscription.priority());
    }
}
