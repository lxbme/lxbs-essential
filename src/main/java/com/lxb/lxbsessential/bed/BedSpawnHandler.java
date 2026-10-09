package com.lxb.lxbsessential.bed;

import com.lxb.lxbsessential.Tags;
import com.lxb.lxbsessential.config.ModConfig;
import com.lxb.lxbsessential.util.Messages;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Right-clicking a bed sets the spawn point at any time of day. Vanilla only sets it on waking up,
 * and in Dregora Bloodmoon, parasites and Comforts' well-rested rule often prevent sleeping.
 * The event is never cancelled, so vanilla sleeping still happens afterwards.
 */
@Mod.EventBusSubscriber(modid = Tags.MOD_ID)
public final class BedSpawnHandler {

    private BedSpawnHandler() {
    }

    /** Lowest priority so protection mods can cancel the click before the spawn point is set. */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!ModConfig.bed.enabled) {
            return;
        }
        World world = event.getWorld();
        EntityPlayer player = event.getEntityPlayer();
        if (world.isRemote || event.getHand() != EnumHand.MAIN_HAND
                || !(player instanceof EntityPlayerMP) || player instanceof FakePlayer) {
            return;
        }
        if (event.getUseBlock() == Event.Result.DENY) {
            return;
        }
        boolean holdingSomething = !player.getHeldItemMainhand().isEmpty() || !player.getHeldItemOffhand().isEmpty();
        if (player.isSneaking() && holdingSomething) {
            return;
        }
        BlockPos pos = event.getPos();
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (!block.isBed(state, world, pos, player)) {
            return;
        }
        ResourceLocation id = block.getRegistryName();
        if (id == null || BlockPatterns.matchesAny(ModConfig.bed.blockBlacklist, id.toString())) {
            return;
        }
        if (!world.provider.canRespawnHere()) {
            return;
        }

        BlockPos head = pos;
        if (block instanceof BlockBed && state.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
            head = pos.offset(state.getValue(BlockHorizontal.FACING));
        }
        int dim = player.dimension;
        BlockPos before = player.getBedLocation(dim);
        player.setSpawnPoint(head, false);
        BlockPos after = player.getBedLocation(dim);
        if (head.equals(after) && !head.equals(before)) {
            player.sendMessage(Messages.spawnSet());
        }
    }
}
