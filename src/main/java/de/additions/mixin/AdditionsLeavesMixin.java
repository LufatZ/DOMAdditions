package de.additions.mixin;

import de.additions.config.AdditionsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * This mixin modifies the behavior of leaf blocks to allow for faster decay.
 * The decay speed is configurable through the {@link AdditionsConfig} class.
 */
@Mixin(LeavesBlock.class)
public abstract class AdditionsLeavesMixin {

    @Shadow @Final public static IntProperty DISTANCE;
    @Shadow @Final public static BooleanProperty PERSISTENT;
    @Shadow @Final public static int MAX_DISTANCE; // = 7

    @Shadow
    protected abstract boolean shouldDecay(BlockState state);

    /**
     * Modifies the delay for leaf decay ticks, based on the mod's configuration.
     * This allows for a configurable, faster leaf decay.
     *
     * @param originalDelay The original delay for the block tick.
     * @return The modified delay, clamped to a minimum of 1.
     */
    @ModifyArg(
            method = "getStateForNeighborUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/tick/ScheduledTickView;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"
            ),
            index = 2
    )
    private int additions$leafDelayClamp(int originalDelay) {
        if (!AdditionsConfig.EnableFastLeafDecay) return originalDelay;
        int cfg = AdditionsConfig.LeafDecayDelay;
        return Math.max(1, cfg);
    }

    /**
     * Accelerates the decay of leaves that are at the maximum distance.
     * This method now includes safeguards to prevent premature decay during chunk generation.
     * It verifies that the leaf block is truly isolated by checking if any neighboring blocks
     * could potentially reduce its distance value.
     *
     * @param state The current block state.
     * @param world The server world.
     * @param pos The position of the block.
     * @param random A random number generator.
     * @param ci The callback info.
     */
    @Inject(method = "scheduledTick", at = @At("TAIL"))
    private void additions$fastDecay(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if (!AdditionsConfig.EnableFastLeafDecay) return;
        if (state.get(PERSISTENT)) return;

        // Only proceed if the block is at max distance after the normal update
        if (state.get(DISTANCE) != MAX_DISTANCE) return;

        // Additional safety check: verify that no neighboring block could reduce the distance
        // This prevents decay during chunk generation when neighbors haven't been processed yet
        if (!isDefinitelyIsolated(world, pos)) return;

        // Use the vanilla shouldDecay check for consistency
        if (this.shouldDecay(state)) {
            Block.dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    /**
     * Checks if a leaf block is definitively isolated from all logs.
     * This method ensures that all neighboring chunks are loaded and that
     * no neighboring block could potentially provide a connection to a log.
     *
     * @param world The server world.
     * @param pos The position to check.
     * @return true if the block is definitively isolated, false otherwise.
     */
    @Unique
    private boolean isDefinitelyIsolated(ServerWorld world, BlockPos pos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (Direction direction : Direction.values()) {
            mutable.set(pos, direction);

            // Check if the chunk is loaded - if not, we can't be sure about isolation
            if (!world.isChunkLoaded(mutable.getX() >> 4, mutable.getZ() >> 4)) {
                return false;
            }

            BlockState neighborState = world.getBlockState(mutable);
            int neighborDistance = LeavesBlock.getOptionalDistanceFromLog(neighborState).orElse(MAX_DISTANCE);

            // If any neighbor has a distance less than max, this block might not be isolated
            if (neighborDistance < MAX_DISTANCE) {
                return false;
            }
        }

        return true;
    }
}