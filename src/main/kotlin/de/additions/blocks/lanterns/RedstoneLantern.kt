package de.additions.blocks.lanterns

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.LanternBlock
import net.minecraft.block.RedstoneTorchBlock
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.block.WireOrientation

/**
 * A redstone-powered version of the standard Lantern block.
 * This block lights up when it receives a redstone signal.
 *
 * @param settings The settings for the block.
 */
open class RedstoneLantern(
    settings: Settings?,
) : LanternBlock(settings) {
    companion object {
        /**
         * A boolean property that determines whether the lantern is lit.
         * This reuses the property from [net.minecraft.block.RedstoneTorchBlock] for consistency.
         */
        val LIT: BooleanProperty = RedstoneTorchBlock.LIT
    }

    init {
        defaultState =
            stateManager.defaultState
                .with(HANGING, false)
                .with(WATERLOGGED, false)
                .with(LIT, false)
    }

    /**
     * Determines the initial state of the block when placed.
     * It sets the hanging state, waterlogged state, and initial lit state based on redstone power.
     *
     * @param ctx The item placement context.
     * @return The appropriate block state for placement, or `null` if it cannot be placed.
     */
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val fluidState = ctx.world.getFluidState(ctx.blockPos)

        for (direction in ctx.placementDirections) {
            if (direction.axis == Direction.Axis.Y) {
                val blockState =
                    defaultState.with(HANGING, direction == Direction.UP)
                if (blockState.canPlaceAt(ctx.world, ctx.blockPos)) {
                    return blockState
                        .with(WATERLOGGED, fluidState.fluid == Fluids.WATER)
                        .with(LIT, ctx.world.isReceivingRedstonePower(ctx.blockPos))
                }
            }
        }

        return null
    }

    /**
     * Handles updates from neighboring blocks, primarily for redstone signals.
     * If the redstone signal changes, it updates the lantern's lit state.
     */
    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        sourceBlock: Block,
        wireOrientation: WireOrientation?,
        notify: Boolean,
    ) {
        if (!world.isClient) {
            val isLit = state.get(LIT)
            if (isLit != world.isReceivingRedstonePower(pos)) {
                if (isLit) {
                    world.scheduleBlockTick(pos, this, 4)
                } else {
                    world.setBlockState(pos, state.cycle(LIT), NOTIFY_LISTENERS)
                }
            }
        }
    }

    /**
     * Handles scheduled block ticks to turn the lantern off after a delay.
     */
    override fun scheduledTick(
        state: BlockState,
        world: ServerWorld,
        pos: BlockPos,
        random: Random,
    ) {
        if (state.get(LIT) && !world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.cycle(LIT), NOTIFY_LISTENERS)
        }
    }

    /**
     * Appends the `LIT` property to the block's state manager.
     */
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(LIT, HANGING, WATERLOGGED)
    }
}