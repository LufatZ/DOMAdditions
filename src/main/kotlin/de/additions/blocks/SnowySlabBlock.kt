package de.additions.blocks

import net.minecraft.block.*
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
 * [SnowySlabBlock] is an extended version of [SlabBlock] that serves as a grass-covered slab.
 *
 * @param settings The block settings (e.g., hardness, tool required).
 */
class SnowySlabBlock(settings: Settings) : SlabBlock(settings) {

    companion object {
        val SNOWY: BooleanProperty = Properties.SNOWY
    }

    init {
        this.defaultState = this.stateManager.defaultState
            .with(SNOWY, false)
            .with(TYPE, SlabType.BOTTOM)
            .with(WATERLOGGED, false)
    }

    /**
     * Adds the snowy property to the block state properties.
     *
     * @param builder The state manager builder.
     */
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(SNOWY)
    }

    /**
     * Sets the placement state of the block.
     *
     * @param ctx The item placement context.
     * @return The block state with the snowy property set.
     */
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val world = ctx.world
        val blockPos = ctx.blockPos
        val aboveBlockState = world.getBlockState(blockPos.up())
        val currentBlockState = world.getBlockState(blockPos)
        val isSnowy = isSnow(aboveBlockState)

        // Check if the block is already a slab and set it to double
        if (currentBlockState.isOf(this)) {
            return currentBlockState.with(TYPE, SlabType.DOUBLE)
                .with(WATERLOGGED, false)
                .with(SNOWY, isSnowy)
        }

        // Determine slab type and waterlogged state
        val fluidState = world.getFluidState(blockPos)
        val isBotom = ctx.side != Direction.DOWN && (ctx.side == Direction.UP || !(ctx.hitPos.y - blockPos.y > 0.5))

        return this.defaultState
            .with(TYPE, if (isBotom) SlabType.BOTTOM else SlabType.TOP)
            .with(WATERLOGGED, fluidState.fluid == Fluids.WATER)
            .with(SNOWY, isSnowy)
    }

    /**
     * Updates the block state when a neighboring block changes.
     *
     * @param state The current block state.
     * @param world The world view.
     * @param tickView The scheduled tick view.
     * @param pos The position of the block.
     * @param direction The direction of the neighbor.
     * @param neighborPos The position of the neighbor block.
     * @param neighborState The state of the neighbor block.
     * @param random The random number generator.
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
        random: Random
    ): BlockState {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world))
        }

        val updatedState = super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random)
        val aboveBlockState = world.getBlockState(pos.up())

        return updatedState.with(SNOWY, isSnow(aboveBlockState))
    }

    /**
     * Checks if the given block state is snow.
     *
     * @param state The block state to check.
     * @return True if the block state is snow, false otherwise.
     */
    private fun isSnow(state: BlockState): Boolean {
        return state.isIn(BlockTags.SNOW)
    }
}