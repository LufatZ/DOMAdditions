package de.additions.blocks

import net.minecraft.block.*
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.StairShape
import net.minecraft.fluid.Fluids
import net.minecraft.item.ItemPlacementContext
import net.minecraft.registry.tag.BlockTags
import net.minecraft.state.StateManager
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.WorldView
import net.minecraft.world.tick.ScheduledTickView

/**
 * [SnowyStairsBlock] is an extended version of [StairsBlock] that serves as a grass-covered staircase.
 *
 * @param blockstate The block state of the base block.
 * @param settings The block settings (e.g., hardness, tool required).
 */
class SnowyStairsBlock(blockstate: BlockState, settings: Settings) : StairsBlock(blockstate, settings) {

    companion object {
        val SNOWY: BooleanProperty = Properties.SNOWY
    }

    init {
        this.defaultState = this.stateManager.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(HALF, BlockHalf.BOTTOM)
            .with(SHAPE, StairShape.STRAIGHT)
            .with(WATERLOGGED, false)
            .with(SNOWY, false)
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
     * This function sets the following properties for the block state:
     * - `SNOWY`: Whether the block above is a snow block.
     * - `FACING`: The direction the player is facing.
     * - `HALF`: Whether the block occupies the top or bottom half of the block space.
     * - `WATERLOGGED`: Whether the block is waterlogged.
     * - `SHAPE`: The shape of the staircase based on neighboring blocks.
     *
     * @param ctx The item placement context.
     * @return The block state with the snowy property set.
     */
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val snowyBlockState = ctx.world.getBlockState(ctx.blockPos.up())
        val direction = ctx.side
        val blockPos = ctx.blockPos
        val fluidState = ctx.world.getFluidState(blockPos)

        return this.defaultState
            .with(SNOWY, isSnow(snowyBlockState))
            .with(FACING, ctx.horizontalPlayerFacing)
            .with(
                HALF,
                if (direction != Direction.DOWN && (direction == Direction.UP || !(ctx.hitPos.y - blockPos.y.toDouble() > 0.5))) {
                    BlockHalf.BOTTOM
                } else {
                    BlockHalf.TOP
                }
            )
            .with(WATERLOGGED, fluidState.fluid == Fluids.WATER)
            .with(SHAPE, getStairShape(this.defaultState, ctx.world, blockPos))
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

        val newState = if (direction.axis.isHorizontal) {
            state.with(SHAPE, getStairShape(state, world, pos))
        } else {
            super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random)
        }

        return newState.with(SNOWY, isSnow(world.getBlockState(pos.up())))
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

    /**
     * Determines the shape of the stair block based on neighboring blocks.
     *
     * @param state The current block state.
     * @param world The world view.
     * @param pos The position of the block.
     * @return The stair shape.
     */
    private fun getStairShape(state: BlockState, world: BlockView, pos: BlockPos): StairShape {
        val direction = state.get(FACING)
        val blockState = world.getBlockState(pos.offset(direction))
        if (blockState.block is StairsBlock && state.get(HALF) == blockState.get(HALF)) {
            val neighborDirection = blockState.get(FACING)
            if (neighborDirection.axis != direction.axis && isDifferentOrientation(state, world, pos, neighborDirection.opposite)) {
                return if (neighborDirection == direction.rotateYCounterclockwise()) {
                    StairShape.OUTER_LEFT
                } else {
                    StairShape.OUTER_RIGHT
                }
            }
        }

        val oppositeBlockState = world.getBlockState(pos.offset(direction.opposite))
        if (oppositeBlockState.block is StairsBlock && state.get(HALF) == oppositeBlockState.get(HALF)) {
            val neighborDirection = oppositeBlockState.get(FACING)
            if (neighborDirection.axis != direction.axis && isDifferentOrientation(state, world, pos, neighborDirection)) {
                return if (neighborDirection == direction.rotateYCounterclockwise()) {
                    StairShape.INNER_LEFT
                } else {
                    StairShape.INNER_RIGHT
                }
            }
        }

        return StairShape.STRAIGHT
    }

    /**
     * Checks if the orientation of the neighboring block is different.
     *
     * @param state The current block state.
     * @param world The world view.
     * @param pos The position of the block.
     * @param dir The direction to check.
     * @return True if the orientation is different, false otherwise.
     */
    private fun isDifferentOrientation(state: BlockState, world: BlockView, pos: BlockPos, dir: Direction): Boolean {
        val neighborState = world.getBlockState(pos.offset(dir))
        return neighborState.block !is StairsBlock || neighborState.get(FACING) != state.get(FACING) || neighborState.get(HALF) != state.get(HALF)
    }
}