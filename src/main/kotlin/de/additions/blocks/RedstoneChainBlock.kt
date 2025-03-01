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
import net.minecraft.world.tick.ScheduledTickView

/**
 * A chain block that conducts redstone power along its axis. When powered, it emits redstone particles
 * and propagates the redstone signal to adjacent blocks in the chain.
 *
 * @property settings Block settings for material, hardness, etc.
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

    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(POWERED, POWER, WATERLOGGED, AXIS)
    }

    /**
     * Gets directions along the specified axis for power calculation.
     *
     * @param axis The axis to get directions for
     * @return Pair of positive and negative directions along the axis
     */
    private fun getAxisDirections(axis: Direction.Axis): Pair<Direction, Direction> =
        when (axis) {
            Direction.Axis.X -> Direction.EAST to Direction.WEST
            Direction.Axis.Y -> Direction.UP to Direction.DOWN
            Direction.Axis.Z -> Direction.NORTH to Direction.SOUTH
        }

    /**
     * Calculates valid redstone power in a specific direction, ignoring same-type blocks with mismatched axes.
     *
     * @param world The world context
     * @param dir Direction to check for power
     * @param pos Current block's pos
     * @param axis Axis of the current block
     * @return Adjusted power level for the direction
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
     * Gets the maximum redstone power available along the block's axis.
     *
     * @param world The world context
     * @param pos Current block position
     * @param axis Axis to check for power
     * @return Pair containing maximum power level and its direction
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

    override fun emitsRedstonePower(state: BlockState): Boolean = state.get(POWERED)

    override fun getWeakRedstonePower(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        direction: Direction,
    ): Int = (state.get(POWER) - 1).coerceAtLeast(0)

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

            world.addParticle(
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
