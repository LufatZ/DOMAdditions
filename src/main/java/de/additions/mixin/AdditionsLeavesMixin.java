package de.additions.mixin;

import de.additions.config.AdditionsConfig;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
     * When fast leaf decay is enabled, this method is called at the end of the scheduled tick,
     * causing leaves to be removed almost instantly if they are at the maximum decay distance.
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
        if (state.get(DISTANCE) == MAX_DISTANCE) {
            Block.dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }
    }
}
