package de.additions.blocks

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.ChainBlock
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.core.particles.DustParticleOptions
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BlockStateProperties.POWER
import net.minecraft.world.level.block.state.properties.BlockStateProperties.POWERED
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.redstone.Orientation

/**
 * A chain block that conducts redstone power along its axis.
 * This block extends the vanilla chain functionality by adding redstone conductivity.
 * When powered, it emits redstone particles and propagates the signal along its axis.
 *
 * @param settings The settings for the block.
 */
open class RedstoneChainBlock(
    settings: Properties,
) : ChainBlock(settings) {
    init {
        // Initialize the default state with powered status, power level, and axis.
        registerDefaultState(
            stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(POWER, 0)
                .setValue(AXIS, Direction.Axis.Y)
        )
    }

    /**
     * Appends the `POWERED` and `POWER` properties to the block's state manager.
     */
    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
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
        world: LevelReader,
        dir: Direction,
        pos: BlockPos,
        axis: Direction.Axis,
    ): Int {
        val neighborPos = pos.relative(dir)
        val nState = world.getBlockState(neighborPos)

        if (nState.block is RedstoneChainBlock && axis != nState.getValue(AXIS)) {
            return 0
        }

        return world.getSignal(neighborPos, dir)
    }

    /**
     * Determines the maximum redstone power available along the block's axis.
     */
    private fun getAxisPower(
        world: LevelReader,
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
    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        val axis = ctx.clickedFace.axis
        val pos = ctx.clickedPos
        val power = getAxisPower(ctx.level, pos, axis)
        return super
            .getStateForPlacement(ctx)
            .setValue(POWER, power)
            .setValue(POWERED, power > 0)
            .setValue(AXIS, axis)
    }

    /**
     * Updates the block's state when a neighboring block changes, recalculating the power level.
     */
    override fun neighborChanged(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        sourceBlock: Block,
        wireOrientation: Orientation?,
        notify: Boolean
    ) {
        val currentAxis = state.getValue(AXIS)
        val power = getAxisPower(world, pos, currentAxis)
        world.setBlockAndUpdate(pos, state.setValue(POWER, power).setValue(POWERED, power > 0))
    }

    /**
     * Returns whether this block can emit redstone power.
     */
    override fun isSignalSource(state: BlockState): Boolean = state.getValue(POWERED)

    /**
     * Gets the weak redstone power output, which is one less than its input power.
     */
    override fun getSignal(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        direction: Direction,
    ): Int = (state.getValue(POWER) - 1).coerceAtLeast(0)

    /**
     * Creates redstone dust particles when the block is powered.
     */
    @Environment(EnvType.CLIENT)
    override fun animateTick(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        random: RandomSource,
    ) {
        val power = state.getValue(POWER)
        if (power == 0 || random.nextInt(5) != 0) return

        val scale = 0.5f + (power / 15.0f * 0.5f)
        val particles = 1 + (power * 4 / 15)

        repeat(particles) {
            val offsets = List(3) { random.nextGaussian() * 0.1 }
            val velocity = List(3) { random.nextGaussian() * 0.02 }

            world.addParticle(
                DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, scale),
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
