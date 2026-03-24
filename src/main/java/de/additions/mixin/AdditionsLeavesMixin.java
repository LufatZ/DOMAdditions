// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
package de.additions.mixin;

import de.additions.config.AdditionsConfig;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
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

    @Shadow @Final public static IntegerProperty DISTANCE;
    @Shadow @Final public static BooleanProperty PERSISTENT;
    @Shadow @Final public static int DECAY_DISTANCE; // = 7

    @Shadow
    protected abstract boolean decaying(BlockState state);

    /**
     * Modifies the delay for leaf decay ticks, based on the mod's configuration.
     * This allows for a configurable, faster leaf decay.
     *
     * @param originalDelay The original delay for the block tick.
     * @return The modified delay, clamped to a minimum of 1.
     */
    @ModifyArg(
            method = "updateShape",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/ScheduledTickAccess;scheduleTick(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/Block;I)V"
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
    @Inject(method = "tick", at = @At("TAIL"))
    private void additions$fastDecay(BlockState state, ServerLevel world, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (!AdditionsConfig.EnableFastLeafDecay) return;
        if (state.getValue(PERSISTENT)) return;

        // Only proceed if the block is at max distance after the normal update
        if (state.getValue(DISTANCE) != DECAY_DISTANCE) return;

        // Additional safety check: verify that no neighboring block could reduce the distance
        // This prevents decay during chunk generation when neighbors haven't been processed yet
        if (!isDefinitelyIsolated(world, pos)) return;

        // Use the vanilla shouldDecay check for consistency
        if (this.decaying(state)) {
            Block.dropResources(state, world, pos);
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
    private boolean isDefinitelyIsolated(ServerLevel world, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);

            // Check if the chunk is loaded - if not, we can't be sure about isolation
            if (!world.hasChunk(mutable.getX() >> 4, mutable.getZ() >> 4)) {
                return false;
            }

            BlockState neighborState = world.getBlockState(mutable);
            int neighborDistance = LeavesBlock.getOptionalDistanceAt(neighborState).orElse(DECAY_DISTANCE);

            // If any neighbor has a distance less than max, this block might not be isolated
            if (neighborDistance < DECAY_DISTANCE) {
                return false;
            }
        }

        return true;
    }
}