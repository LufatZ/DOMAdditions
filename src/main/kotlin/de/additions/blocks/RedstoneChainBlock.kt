@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.blocks

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ChainBlock
import net.minecraft.item.ItemPlacementContext
import net.minecraft.particle.DustParticleEffect
import net.minecraft.state.StateManager
import net.minecraft.state.property.Properties.POWER
import net.minecraft.state.property.Properties.POWERED
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.BlockView
import net.minecraft.world.World
import net.minecraft.world.WorldView
import net.minecraft.world.block.WireOrientation

/**
 * A specialized chain block that conducts redstone power along its axis.
 * This block extends the vanilla Chain Block functionality by adding redstone conductivity.
 * When powered, it emits redstone particles and propagates power signals to adjacent blocks
 * along its axis, creating a directional power transmission system.
 *
 * Features:
 * - Conducts redstone power only along its placed axis (X, Y, or Z)
 * - Power decreases by 1 for each block in the chain (similar to redstone dust)
 * - Displays power level with redstone particles when active
 * - Only connects to other RedstoneChain blocks with matching axes
 *
 * @property settings Block settings for material, hardness, sound, etc.
 */
class RedstoneChainBlock(
    settings: Settings,
) : ChainBlock(settings) {
    init {
        // Initialize default state with powered status, power level, and vertical axis
        defaultState =
            stateManager.defaultState
                .with(POWERED, false)
                .with(POWER, 0)
                .with(AXIS, Direction.Axis.Y)
    }

    /**
     * Registers block properties to the state manager.
     * Adds POWERED and POWER properties in addition to the inherited WATERLOGGED and AXIS properties.
     *
     * @param builder State manager builder to register properties to
     */
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(POWERED, POWER, WATERLOGGED, AXIS)
    }

    /**
     * Determines the two directions along a specific axis.
     * Used to check both directions along an axis for power sources.
     *
     * @param axis The axis (X, Y, or Z) to get directions for
     * @return Pair containing positive and negative directions along the specified axis
     */
    private fun getAxisDirections(axis: Direction.Axis): Pair<Direction, Direction> =
        when (axis) {
            Direction.Axis.X -> Direction.EAST to Direction.WEST
            Direction.Axis.Y -> Direction.UP to Direction.DOWN
            Direction.Axis.Z -> Direction.NORTH to Direction.SOUTH
        }

    /**
     * Calculates valid redstone power in a specific direction.
     * Ignores RedstoneChain blocks with mismatched axes to prevent cross-axis connections.
     *
     * @param world The world context for retrieving block states
     * @param dir Direction to check for power input
     * @param pos Current block's position
     * @param axis Axis of the current block
     * @return The adjusted power level from the specified direction (0 if invalid)
     */
    private fun getValidDirectionPower(
        world: WorldView,
        dir: Direction,
        pos: BlockPos,
        axis: Direction.Axis,
    ): Int {
        val neighborPos = pos.offset(dir)
        val nState = world.getBlockState(neighborPos)

        // Ignores all redstone chains with mismatched axes
        if (nState.block is RedstoneChainBlock) {
            if (axis != nState.get(AXIS)) {
                return 0
            }
        }

        return world.getEmittedRedstonePower(neighborPos, dir)
    }

    /**
     * Determines the maximum redstone power available along the block's axis.
     * Checks both directions along the axis and returns the higher power value.
     *
     * @param world The world context for block state queries
     * @param pos Current block position
     * @param axis Axis to check for power
     * @return The maximum power level found along the block's axis
     */
    private fun getAxisPower(
        world: WorldView,
        pos: BlockPos,
        axis: Direction.Axis,
    ): Int {
        val (dir1, dir2) = getAxisDirections(axis)
        val power1 = getValidDirectionPower(world, dir1, pos, axis)
        val power2 = getValidDirectionPower(world, dir2, pos, axis)

        return if (power1 >= power2) power1 else power2
    }

    /**
     * Determines the initial block state when placed in the world.
     * Sets the appropriate axis based on placement direction and calculates initial power level.
     *
     * @param ctx The item placement context containing placement information
     * @return The block state to use when the block is first placed
     */
    override fun getPlacementState(ctx: ItemPlacementContext): BlockState {
        val axis = ctx.side.axis
        val pos = ctx.blockPos
        val power = getAxisPower(ctx.world, pos, axis)
        return super
            .getPlacementState(ctx)!!
            .with(POWER, power)
            .with(POWERED, power > 0)
            .with(AXIS, axis)
    }

    /**
     * Updates the block's state when a neighboring block changes.
     * Recalculates power level and updates the block state accordingly.
     *
     * @param state The current block state
     * @param world The world context
     * @param pos The position of this block
     * @param sourceBlock The block that caused the update
     * @param wireOrientation The orientation of connecting wires (if applicable)
     * @param notify Whether to notify neighbors of the change
     */
    override fun neighborUpdate( // potentially wrong method
        state: BlockState?,
        world: World?,
        pos: BlockPos?,
        sourceBlock: Block?,
        wireOrientation: WireOrientation?,
        notify: Boolean,
    ) {
        val currentAxis = state?.get(AXIS)
        val power = getAxisPower(world!!, pos!!, currentAxis!!)
        world.setBlockState(pos, state.with(POWER, power).with(POWERED, power > 0))
    }

    /**
     * Determines whether this block can emit redstone power.
     * Returns true if the block is in a powered state.
     *
     * @param state The block state to check
     * @return true if the block can emit redstone power, false otherwise
     */
    override fun emitsRedstonePower(state: BlockState): Boolean = state.get(POWERED)

    /**
     * Calculates the weak redstone power output in a specific direction.
     * Decreases power by 1 compared to input power, similar to redstone dust.
     *
     * @param state The current block state
     * @param world The block view context
     * @param pos The position of this block
     * @param direction The direction to calculate power for
     * @return The power level emitted in the specified direction (power - 1, minimum 0)
     */
    override fun getWeakRedstonePower(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        direction: Direction,
    ): Int = (state.get(POWER) - 1).coerceAtLeast(0)

    /**
     * Creates visual particle effects when the block is powered.
     * Generates red dust particles proportional to the power level.
     * Only runs on the client side.
     *
     * @param state The current block state
     * @param world The world context
     * @param pos The position of this block
     * @param random Random number generator for particle effects
     */
    @Environment(EnvType.CLIENT)
    override fun randomDisplayTick(
        state: BlockState,
        world: World,
        pos: BlockPos,
        random: Random,
    ) {
        // Early exit for non-powered state
        val power = state.get(POWER)
        if (power == 0 || random.nextInt(5) != 0) return

        // Calculate particle parameters
        val scale = 0.5f + (power / 15.0f * 0.5f)
        val particles = 1 + (power * 4 / 15)

        repeat(particles) {
            // Generate positions with normal distribution around center
            val offsets = List(3) { random.nextGaussian() * 0.1 }
            val velocity = List(3) { random.nextGaussian() * 0.02 }

            world.addParticleClient(
                DustParticleEffect(0xFF0000, scale),
                pos.x + 0.5 + offsets[0],
                pos.y + 0.5 + offsets[1],
                pos.z + 0.5 + offsets[2],
                velocity[0],
                velocity[1],
                velocity[2],
            )
        }
    }
}
