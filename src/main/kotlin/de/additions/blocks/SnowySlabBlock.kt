package de.additions.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.SlabBlock
import net.minecraft.block.enums.SlabType
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.registry.tag.BlockTags
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.WorldView
import net.minecraft.world.tick.ScheduledTickView

/**
 * Represents a slab block that can be covered with snow, similar to a grass block.
 * This block changes its appearance based on the block above it.
 *
 * @param settings The block settings (e.g., hardness, tool required).
 */
class SnowySlabBlock(
    settings: Settings,
) : SlabBlock(settings) {
    companion object {
        /** A boolean property indicating whether the slab is covered with snow. */
        val SNOWY: BooleanProperty = Properties.SNOWY
    }

    init {
        this.defaultState =
            this.stateManager.defaultState
                .with(SNOWY, false)
                .with(TYPE, SlabType.BOTTOM)
                .with(WATERLOGGED, false)
    }

    /**
     * Appends the `SNOWY` property to the block's state manager.
     *
     * @param builder The state manager builder.
     */
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(SNOWY)
    }

    /**
     * Determines the block state upon placement.
     * The `SNOWY` state is set based on whether a snow block is placed on top.
     *
     * @param ctx The item placement context.
     * @return The appropriate block state for placement.
     */
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val world = ctx.world
        val blockPos = ctx.blockPos
        val aboveBlockState = world.getBlockState(blockPos.up())
        val currentBlockState = world.getBlockState(blockPos)
        val isSnowy = isSnow(aboveBlockState)

        // If the block is being placed on an existing slab, create a double slab
        if (currentBlockState.isOf(this)) {
            return currentBlockState
                .with(TYPE, SlabType.DOUBLE)
                .with(WATERLOGGED, false)
                .with(SNOWY, isSnowy)
        }

        // Determine slab type (top/bottom) and waterlogged state
        val fluidState = world.getFluidState(blockPos)
        val isBottom = ctx.side != Direction.DOWN && (ctx.side == Direction.UP || !(ctx.hitPos.y - blockPos.y > 0.5))

        return this.defaultState
            .with(TYPE, if (isBottom) SlabType.BOTTOM else SlabType.TOP)
            .with(WATERLOGGED, fluidState.fluid == Fluids.WATER)
            .with(SNOWY, isSnowy)
    }

    /**
     * Updates the block state when a neighboring block changes.
     * This is used to update the `SNOWY` state if a snow block is placed or removed from above.
     *
     * @return The updated block state.
     */
    override fun getStateForNeighborUpdate(
        state: BlockState,
        world: WorldView,
        tickView: ScheduledTickView,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: Random,
    ): BlockState {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        }

        val updatedState = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random)
        val aboveBlockState = world.getBlockState(pos.up())

        return updatedState.with(SNOWY, isSnow(aboveBlockState))
    }

    /**
     * Checks if the given block state is a snow block.
     *
     * @param state The block state to check.
     * @return `true` if the block state is in the `SNOW` tag, `false` otherwise.
     */
    private fun isSnow(state: BlockState): Boolean = state.isIn(BlockTags.SNOW)
}
