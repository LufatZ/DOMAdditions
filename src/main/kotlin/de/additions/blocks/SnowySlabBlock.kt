package de.additions.blocks

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess

/**
 * Represents a slab block that can be covered with snow, similar to a grass block.
 * This block changes its appearance based on the block above it.
 *
 * @param settings The block settings (e.g., hardness, tool required).
 */
class SnowySlabBlock(
    settings: Properties,
) : SlabBlock(settings) {
    companion object {
        /** A boolean property indicating whether the slab is covered with snow. */
        val SNOWY: BooleanProperty = BlockStateProperties.SNOWY
    }

    init {
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(SNOWY, false)
                .setValue(TYPE, SlabType.BOTTOM)
                .setValue(WATERLOGGED, false)
        )
    }

    /**
     * Appends the `SNOWY` property to the block's state manager.
     *
     * @param builder The state manager builder.
     */
    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(SNOWY)
    }

    /**
     * Determines the block state upon placement.
     * The `SNOWY` state is set based on whether a snow block is placed on top.
     *
     * @param ctx The item placement context.
     * @return The appropriate block state for placement.
     */
    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        val world = ctx.level
        val blockPos = ctx.clickedPos
        val aboveBlockState = world.getBlockState(blockPos.above())
        val currentBlockState = world.getBlockState(blockPos)
        val isSnowy = isSnow(aboveBlockState)

        // If the block is being placed on an existing slab, create a double slab
        if (currentBlockState.`is`(this)) {
            return currentBlockState
                .setValue(TYPE, SlabType.DOUBLE)
                .setValue(WATERLOGGED, false)
                .setValue(SNOWY, isSnowy)
        }

        // Determine slab type (top/bottom) and waterlogged state
        val fluidState = world.getFluidState(blockPos)
        val isBottom = ctx.clickedFace != Direction.DOWN && (ctx.clickedFace == Direction.UP || !(ctx.clickLocation.y - blockPos.y > 0.5))

        return this.defaultBlockState()
            .setValue(TYPE, if (isBottom) SlabType.BOTTOM else SlabType.TOP)
            .setValue(WATERLOGGED, fluidState.type == Fluids.WATER)
            .setValue(SNOWY, isSnowy)
    }

    /**
     * Updates the block state when a neighboring block changes.
     * This is used to update the `SNOWY` state if a snow block is placed or removed from above.
     *
     * @return The updated block state.
     */
    override fun updateShape(
        state: BlockState,
        world: LevelReader,
        tickView: ScheduledTickAccess,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: RandomSource,
    ): BlockState {
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world))
        }

        val updatedState = super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random)
        val aboveBlockState = world.getBlockState(pos.above())

        return updatedState.setValue(SNOWY, isSnow(aboveBlockState))
    }

    /**
     * Checks if the given block state is a snow block.
     *
     * @param state The block state to check.
     * @return `true` if the block state is in the `SNOW` tag, `false` otherwise.
     */
    private fun isSnow(state: BlockState): Boolean = state.`is`(BlockTags.SNOW)
}
