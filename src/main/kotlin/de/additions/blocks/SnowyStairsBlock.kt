package de.additions.blocks

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.properties.Half
import net.minecraft.world.level.block.state.properties.StairsShape
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess

/**
 * Represents a stair block that can be covered with snow, similar to a grass block.
 * This block changes its appearance based on the block above it.
 *
 * @param blockstate The base block state for the stair.
 * @param settings The block settings (e.g., hardness, tool required).
 */
class SnowyStairsBlock(
    blockstate: BlockState,
    settings: Properties,
) : StairBlock(blockstate, settings) {
    companion object {
        /** A boolean property indicating whether the stair is covered with snow. */
        val SNOWY: BooleanProperty = BlockStateProperties.SNOWY
    }

    init {
        this.registerDefaultState(
            this.stateDefinition
                .any()
                .setValue(FACING, Direction.NORTH)
                .setValue(HALF, Half.BOTTOM)
                .setValue(SHAPE, StairsShape.STRAIGHT)
                .setValue(WATERLOGGED, false)
                .setValue(SNOWY, false)
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
        val snowyBlockState = ctx.level.getBlockState(ctx.clickedPos.above())
        val direction = ctx.clickedFace
        val blockPos = ctx.clickedPos
        val fluidState = ctx.level.getFluidState(blockPos)
        val blockState =
            this.defaultBlockState()
                .setValue(SNOWY, isSnow(snowyBlockState))
                .setValue(FACING, ctx.horizontalDirection)
                .setValue(
                    HALF,
                    if (direction != Direction.DOWN && (direction == Direction.UP || !(ctx.clickLocation.y - blockPos.y.toDouble() > 0.5))) {
                        Half.BOTTOM
                    } else {
                        Half.TOP
                    },
                ).setValue(WATERLOGGED, fluidState.type == Fluids.WATER)

        return blockState.setValue(SHAPE, getStairShape(blockState, ctx.level, blockPos))
    }

    /**
     * Updates the block state when a neighboring block changes.
     * This is used to update the `SNOWY` state and the stair shape.
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
        val newState =
            if (direction.axis.isHorizontal) {
                state.setValue(SHAPE, getStairShape(state, world, pos))
            } else {
                super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random)
            }

        return newState.setValue(SNOWY, isSnow(world.getBlockState(pos.above())))
    }

    /**
     * Checks if the given block state is a snow block.
     *
     * @param state The block state to check.
     * @return `true` if the block state is in the `SNOW` tag, `false` otherwise.
     */
    private fun isSnow(state: BlockState): Boolean = state.`is`(BlockTags.SNOW)

    /**
     * Determines the shape of the stair block based on its neighbors.
     *
     * @param state The current block state.
     * @param world The world view.
     * @param pos The position of the block.
     * @return The calculated [StairsShape].
     */
    private fun getStairShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
    ): StairsShape {
        val direction = state.getValue(FACING)
        val blockState = world.getBlockState(pos.relative(direction))
        if (isStairs(blockState) && state.getValue(HALF) == blockState.getValue(HALF)) {
            val neighborDirection = blockState.getValue(FACING)
            if (neighborDirection.axis != direction.axis && isDifferentOrientation(state, world, pos, neighborDirection.opposite)) {
                return if (neighborDirection == direction.counterClockWise) {
                    StairsShape.OUTER_LEFT
                } else {
                    StairsShape.OUTER_RIGHT
                }
            }
        }

        val oppositeBlockState = world.getBlockState(pos.relative(direction.opposite))
        if (isStairs(oppositeBlockState) && state.getValue(HALF) == oppositeBlockState.getValue(HALF)) {
            val neighborDirection = oppositeBlockState.getValue(FACING)
            if (neighborDirection.axis != direction.axis && isDifferentOrientation(state, world, pos, neighborDirection)) {
                return if (neighborDirection == direction.counterClockWise) {
                    StairsShape.INNER_LEFT
                } else {
                    StairsShape.INNER_RIGHT
                }
            }
        }

        return StairsShape.STRAIGHT
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
        world: BlockGetter,
        pos: BlockPos,
        dir: Direction,
    ): Boolean {
        val neighborState = world.getBlockState(pos.relative(dir))
        return !isStairs(neighborState) ||
            neighborState.getValue(FACING) != state.getValue(FACING) ||
            neighborState.getValue(HALF) != state.getValue(HALF)
    }
}
