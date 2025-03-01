package de.additions.blocks

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
import org.jetbrains.annotations.Nullable

/**
 * A redstone-powered version of the standard Lantern block.
 * This block behaves like a Redstone Lamp but maintains the appearance of a Lantern.
 * The lantern will light up when receiving a redstone signal and turn off when the signal is removed.
 */
class RedstoneLantern(
    settings: Settings?,
) : LanternBlock(settings) {
    companion object {
        /**
         * Boolean property that determines whether the lantern is lit.
         * Reuses the same property from RedstoneTorchBlock for compatibility.
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
     * Determines the initial state of the block when placed in the world.
     * Handles placement orientation (hanging vs standing) and checks if the block should
     * be waterlogged. Also sets the initial lit state based on redstone power.
     *
     * @param ctx The context containing information about the placement
     * @return The block state to use when placing the block, or null if it can't be placed
     */
    @Nullable
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
        val fluidState = ctx.world.getFluidState(ctx.blockPos)

        // Check all possible placement directions
        for (direction in ctx.placementDirections) {
            if (direction.axis == Direction.Axis.Y) {
                // Set hanging state based on whether the direction is UP
                val blockState =
                    defaultState.with(HANGING, direction == Direction.UP)
                if (blockState.canPlaceAt(ctx.world, ctx.blockPos)) {
                    return blockState
                        // Set waterlogged state if placed in water
                        .with(WATERLOGGED, fluidState.fluid == Fluids.WATER)
                        // Set initial lit state based on redstone power
                        .with(LIT, ctx.world.isReceivingRedstonePower(ctx.blockPos))
                }
            }
        }

        return null
    }

    /**
     * Handles updates from neighboring blocks, particularly for redstone signals.
     * If the redstone signal state has changed, schedules a block tick or
     * immediately updates the block state.
     *
     * @param state The current state of this block
     * @param world The world the block is in
     * @param pos The position of this block
     * @param sourceBlock The block that triggered the update
     * @param wireOrientation The orientation of connected redstone wire (if applicable)
     * @param notify Whether to notify neighbors of the change
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
            val bl = state.get(LIT)
            if (bl != world.isReceivingRedstonePower(pos)) {
                // If there's a change in redstone signal, schedule a block tick
                if (bl) {
                    world.scheduleBlockTick(pos, this, 4)
                } else {
                    // Otherwise toggle the lit state
                    world.setBlockState(pos, state.with(LIT, world.isReceivingRedstonePower(pos)), Block.NOTIFY_LISTENERS)
                }
            }
        }
    }

    /**
     * Handles scheduled block ticks, primarily used for delayed state changes.
     * This is used to implement a delay when turning off the lantern.
     *
     * @param state The current state of this block
     * @param world The server world the block is in
     * @param pos The position of this block
     * @param random A random number generator
     */
    override fun scheduledTick(
        state: BlockState,
        world: ServerWorld,
        pos: BlockPos,
        random: Random,
    ) {
        // If the lantern is lit but not receiving power, turn it off
        if (state.get(LIT) && !world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.with(LIT, world.isReceivingRedstonePower(pos)), Block.NOTIFY_LISTENERS)
        }
    }

    /**
     * Adds the block's properties to the state manager.
     * This adds the 'lit' property to the standard lantern properties.
     *
     * @param builder The state manager builder to add properties to
     */
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(LIT, HANGING, WATERLOGGED)
    }
}
