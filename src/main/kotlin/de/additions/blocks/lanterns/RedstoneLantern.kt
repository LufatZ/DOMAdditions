package de.additions.blocks.lanterns

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.LanternBlock
import net.minecraft.world.level.block.RedstoneTorchBlock
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.redstone.Orientation

/**
 * A redstone-powered version of the standard Lantern block.
 * This block lights up when it receives a redstone signal.
 *
 * @param settings The settings for the block.
 */
open class RedstoneLantern(
    settings: Properties,
) : LanternBlock(settings) {
    companion object {
        /**
         * A boolean property that determines whether the lantern is lit.
         * This reuses the property from [RedstoneTorchBlock] for consistency.
         */
        val LIT: BooleanProperty = RedstoneTorchBlock.LIT
    }

    init {
        registerDefaultState(
            stateDefinition.any()
                .setValue(HANGING, false)
                .setValue(WATERLOGGED, false)
                .setValue(LIT, false)
        )
    }

    /**
     * Determines the initial state of the block when placed.
     * It sets the hanging state, waterlogged state, and initial lit state based on redstone power.
     *
     * @param ctx The item placement context.
     * @return The appropriate block state for placement, or `null` if it cannot be placed.
     */
    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
        val fluidState = ctx.level.getFluidState(ctx.clickedPos)

        for (direction in ctx.nearestLookingDirections) {
            if (direction.axis == Direction.Axis.Y) {
                val blockState =
                    defaultBlockState().setValue(HANGING, direction == Direction.UP)
                if (blockState.canSurvive(ctx.level, ctx.clickedPos)) {
                    return blockState
                        .setValue(WATERLOGGED, fluidState.type == Fluids.WATER)
                        .setValue(LIT, ctx.level.hasNeighborSignal(ctx.clickedPos))
                }
            }
        }

        return null
    }

    /**
     * Handles updates from neighboring blocks, primarily for redstone signals.
     * If the redstone signal changes, it updates the lantern's lit state.
     */
    override fun neighborChanged(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        sourceBlock: Block,
        wireOrientation: Orientation?,
        notify: Boolean,
    ) {
        if (!world.isClientSide) {
            val isLit = state.getValue(LIT)
            if (isLit != world.hasNeighborSignal(pos)) {
                if (isLit) {
                    world.scheduleTick(pos, this, 4)
                } else {
                    world.setBlock(pos, state.cycle(LIT), UPDATE_CLIENTS)
                }
            }
        }
    }

    /**
     * Handles scheduled block ticks to turn the lantern off after a delay.
     */
    override fun tick(
        state: BlockState,
        world: ServerLevel,
        pos: BlockPos,
        random: RandomSource,
    ) {
        if (state.getValue(LIT) && !world.hasNeighborSignal(pos)) {
            world.setBlock(pos, state.cycle(LIT), UPDATE_CLIENTS)
        }
    }

    /**
     * Appends the `LIT` property to the block's state manager.
     */
    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(LIT, HANGING, WATERLOGGED)
    }
}