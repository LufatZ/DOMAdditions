package de.additions.blocks

import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
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
 * Represents a stair block that can be covered with snow, similar to a grass block.
 * This block changes its appearance based on the block above it.
 *
 * @param blockstate The base block state for the stair.
 * @param settings The block settings (e.g., hardness, tool required).
 */
class SnowyStairsBlock(
    blockstate: BlockState,
    settings: Settings,
) : StairsBlock(blockstate, settings) {
    companion object {
        /** A boolean property indicating whether the stair is covered with snow. */
        val SNOWY: BooleanProperty = Properties.SNOWY
    }

    init {
        this.defaultState =
            this.stateManager
                .defaultState
                .with(FACING, Direction.NORTH)
                .with(HALF, BlockHalf.BOTTOM)
                .with(SHAPE, StairShape.STRAIGHT)
                .with(WATERLOGGED, false)
                .with(SNOWY, false)
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
        val snowyBlockState = ctx.world.getBlockState(ctx.blockPos.up())
        val direction = ctx.side
        val blockPos = ctx.blockPos
        val fluidState = ctx.world.getFluidState(blockPos)
        val blockState =
            this.defaultState
                .with(SNOWY, isSnow(snowyBlockState))
                .with(FACING, ctx.horizontalPlayerFacing)
                .with(
                    HALF,
                    if (direction != Direction.DOWN && (direction == Direction.UP || !(ctx.hitPos.y - blockPos.y.toDouble() > 0.5))) {
                        BlockHalf.BOTTOM
                    } else {
                        BlockHalf.TOP
                    },
                ).with(WATERLOGGED, fluidState.fluid == Fluids.WATER)

        return blockState.with(SHAPE, getStairShape(blockState, ctx.world, blockPos))
    }

    /**
     * Updates the block state when a neighboring block changes.
     * This is used to update the `SNOWY` state and the stair shape.
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
        val newState =
            if (direction.axis.isHorizontal) {
                state.with(SHAPE, getStairShape(state, world, pos))
            } else {
                super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random)
            }

        return newState.with(SNOWY, isSnow(world.getBlockState(pos.up())))
    }

    /**
     * Checks if the given block state is a snow block.
     *
     * @param state The block state to check.
     * @return `true` if the block state is in the `SNOW` tag, `false` otherwise.
     */
    private fun isSnow(state: BlockState): Boolean = state.isIn(BlockTags.SNOW)

    /**
     * Determines the shape of the stair block based on its neighbors.
     *
     * @param state The current block state.
     * @param world The world view.
     * @param pos The position of the block.
     * @return The calculated [StairShape].
     */
    private fun getStairShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
    ): StairShape {
        val direction = state.get(FACING)
        val blockState = world.getBlockState(pos.offset(direction))
        if (isStairs(blockState) && state.get(HALF) == blockState.get(HALF)) {
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
        if (isStairs(oppositeBlockState) && state.get(HALF) == oppositeBlockState.get(HALF)) {
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
     * Checks if a neighboring stair has a different orientation.
     *
     * @param state The current block state.
     * @param world The world view.
     * @param pos The position of the block.
     * @param dir The direction to check.
     * @return `true` if the orientation is different, `false` otherwise.
     */
    private fun isDifferentOrientation(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        dir: Direction,
    ): Boolean {
        val neighborState = world.getBlockState(pos.offset(dir))
        return !isStairs(neighborState) ||
            neighborState.get(FACING) != state.get(FACING) ||
            neighborState.get(HALF) != state.get(HALF)
    }
}
