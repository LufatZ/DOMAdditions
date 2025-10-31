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
 * A chain block that conducts redstone power along its axis.
 * This block extends the vanilla chain functionality by adding redstone conductivity.
 * When powered, it emits redstone particles and propagates the signal along its axis.
 *
 * @param settings The settings for the block.
 */
open class RedstoneChainBlock(
    settings: Settings,
) : ChainBlock(settings) {
    init {
        // Initialize the default state with powered status, power level, and axis.
        defaultState =
            stateManager.defaultState
                .with(POWERED, false)
                .with(POWER, 0)
                .with(AXIS, Direction.Axis.Y)
    }

    /**
     * Appends the `POWERED` and `POWER` properties to the block's state manager.
     */
    override fun appendProperties(builder: StateManager.Builder<Block, BlockState>) {
        builder.add(POWERED, POWER, WATERLOGGED, AXIS)
    }

    /**
     * Gets the two directions along a specific axis.
     */
    private fun getAxisDirections(axis: Direction.Axis): Pair<Direction, Direction> =
        when (axis) {
            Direction.Axis.X -> Direction.EAST to Direction.WEST
            Direction.Axis.Y -> Direction.UP to Direction.DOWN
            Direction.Axis.Z -> Direction.NORTH to Direction.SOUTH
        }

    /**
     * Calculates the valid redstone power from a specific direction, ignoring other redstone chains with a mismatched axis.
     */
    private fun getValidDirectionPower(
        world: WorldView,
        dir: Direction,
        pos: BlockPos,
        axis: Direction.Axis,
    ): Int {
        val neighborPos = pos.offset(dir)
        val nState = world.getBlockState(neighborPos)

        if (nState.block is RedstoneChainBlock && axis != nState.get(AXIS)) {
            return 0
        }

        return world.getEmittedRedstonePower(neighborPos, dir)
    }

    /**
     * Determines the maximum redstone power available along the block's axis.
     */
    private fun getAxisPower(
        world: WorldView,
        pos: BlockPos,
        axis: Direction.Axis,
    ): Int {
        val (dir1, dir2) = getAxisDirections(axis)
        val power1 = getValidDirectionPower(world, dir1, pos, axis)
        val power2 = getValidDirectionPower(world, dir2, pos, axis)

        return maxOf(power1, power2)
    }

    /**
     * Determines the initial block state upon placement, setting the axis and initial power level.
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
     * Updates the block's state when a neighboring block changes, recalculating the power level.
     */
    override fun neighborUpdate(
        state: BlockState,
        world: World,
        pos: BlockPos,
        sourceBlock: Block,
        wireOrientation: WireOrientation?,
        notify: Boolean
    ) {
        val currentAxis = state.get(AXIS)
        val power = getAxisPower(world, pos, currentAxis)
        world.setBlockState(pos, state.with(POWER, power).with(POWERED, power > 0))
    }

    /**
     * Returns whether this block can emit redstone power.
     */
    override fun emitsRedstonePower(state: BlockState): Boolean = state.get(POWERED)

    /**
     * Gets the weak redstone power output, which is one less than its input power.
     */
    override fun getWeakRedstonePower(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        direction: Direction,
    ): Int = (state.get(POWER) - 1).coerceAtLeast(0)

    /**
     * Creates redstone dust particles when the block is powered.
     */
    @Environment(EnvType.CLIENT)
    override fun randomDisplayTick(
        state: BlockState,
        world: World,
        pos: BlockPos,
        random: Random,
    ) {
        val power = state.get(POWER)
        if (power == 0 || random.nextInt(5) != 0) return

        val scale = 0.5f + (power / 15.0f * 0.5f)
        val particles = 1 + (power * 4 / 15)

        repeat(particles) {
            val offsets = List(3) { random.nextGaussian() * 0.1 }
            val velocity = List(3) { random.nextGaussian() * 0.02 }

            world.addParticleClient(
                DustParticleEffect(DustParticleEffect.RED, scale),
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
