package de.additions.blocks

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.properties.Half
import net.minecraft.world.level.block.state.properties.StairsShape
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

/**
 * Represents a stair block variant of Crying Obsidian.
 * This stair emits obsidian tear particles from its edges, with behavior dependent on the stair's shape and orientation.
 *
 * @param baseBlockState The base block state this stair is modeled after.
 * @param settings The settings for the block.
 */
class CryingObsidianStair(
    baseBlockState: BlockState,
    settings: Properties,
) : StairBlock(baseBlockState, settings) {
    /**
     * Called periodically to display random particles.
     * There is a 20% chance each tick for particles to spawn.
     * The particle emission logic is delegated based on the stair's properties.
     */
    override fun animateTick(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        random: RandomSource,
    ) {
        if (random.nextInt(5) == 0) {
            val facing = state.getValue(FACING)
            val half = state.getValue(HALF)
            val shape = state.getValue(SHAPE)

            getValidDirections(facing, half, shape).forEach { direction ->
                trySpawnParticle(world, pos, direction, random, half)
            }
        }
    }

    /**
     * Determines the valid directions for particle emission based on the stair's configuration.
     *
     * @param facing The primary facing direction of the stair.
     * @param half Whether the stair is in the top or bottom half.
     * @param shape The stair's geometric shape (e.g., straight, inner, outer).
     * @return A list of valid emission directions.
     */
    private fun getValidDirections(
        facing: Direction,
        half: Half,
        shape: StairsShape,
    ): List<Direction> =
        when (half) {
            Half.BOTTOM -> getBottomValidDirections(facing, shape)
            Half.TOP -> getTopValidDirections(facing, shape)
        }

    /**
     * Calculates the emission directions for a bottom-half stair.
     */
    private fun getBottomValidDirections(
        facing: Direction,
        shape: StairsShape,
    ): List<Direction> {
        val directions = mutableListOf(Direction.DOWN)
        when (shape) {
            StairsShape.STRAIGHT -> directions.add(facing.opposite)
            StairsShape.INNER_LEFT,
            StairsShape.OUTER_LEFT,
            -> {
                directions.add(facing.counterClockWise)
                directions.add(facing.opposite)
            }
            StairsShape.INNER_RIGHT,
            StairsShape.OUTER_RIGHT,
            -> {
                directions.add(facing.clockWise)
                directions.add(facing.opposite)
            }
        }
        return directions
    }

    /**
     * Calculates the emission directions for a top-half stair.
     */
    private fun getTopValidDirections(
        facing: Direction,
        shape: StairsShape,
    ): List<Direction> {
        val directions = mutableListOf(Direction.UP)
        when (shape) {
            StairsShape.STRAIGHT -> directions.add(facing)
            StairsShape.INNER_LEFT,
            StairsShape.OUTER_LEFT,
            -> {
                directions.add(facing)
                directions.add(facing.counterClockWise)
            }
            StairsShape.INNER_RIGHT,
            StairsShape.OUTER_RIGHT,
            -> {
                directions.add(facing)
                directions.add(facing.clockWise)
            }
        }
        return directions
    }

    /**
     * Attempts to spawn a particle if the adjacent block side is not opaque.
     *
     * @param half The stair half (top/bottom) for vertical positioning.
     */
    private fun trySpawnParticle(
        world: Level,
        pos: BlockPos,
        direction: Direction,
        random: RandomSource,
        half: Half,
    ) {
        val blockPos = pos.relative(direction)
        val blockState = world.getBlockState(blockPos)

        if (!blockState.canOcclude() || !blockState.isFaceSturdy(world, blockPos, direction.opposite)) {
            val (x, y, z) = calculateParticlePosition(direction, random, half)
            world.addParticle(
                ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                pos.x + x,
                pos.y + y,
                pos.z + z,
                0.0,
                0.0,
                0.0,
            )
        }
    }

    /**
     * Calculates the precise position of a particle within the block's boundaries.
     *
     * @param direction The direction of particle emission.
     * @param half The stair half for vertical positioning.
     * @return A [Triple] containing the X, Y, and Z offsets.
     */
    private fun calculateParticlePosition(
        direction: Direction,
        random: RandomSource,
        half: Half,
    ): Triple<Double, Double, Double> {
        val (baseY, heightRange) =
            when (half) {
                Half.BOTTOM -> 0.25 to 0.25
                Half.TOP -> 0.5 to 0.5
            }

        return when (direction.axis) {
            Direction.Axis.Y -> handleVerticalDirection(direction, random, half)
            else -> handleHorizontalDirection(direction, random, baseY, heightRange)
        }
    }

    /**
     * Handles vertical (up/down) particle positioning.
     */
    private fun handleVerticalDirection(
        direction: Direction,
        random: RandomSource,
        half: Half,
    ): Triple<Double, Double, Double> {
        val y =
            when {
                half == Half.BOTTOM && direction == Direction.DOWN -> 0.1 + random.nextDouble() * 0.4
                half == Half.TOP && direction == Direction.UP -> 0.9 + random.nextDouble() * 0.1
                else -> random.nextDouble()
            }

        return Triple(
            random.nextDouble(),
            y,
            random.nextDouble(),
        )
    }

    /**
     * Handles horizontal particle positioning.
     */
    private fun handleHorizontalDirection(
        direction: Direction,
        random: RandomSource,
        baseY: Double,
        heightRange: Double,
    ): Triple<Double, Double, Double> {
        val (x, z) =
            when (direction) {
                Direction.NORTH -> Pair(random.nextDouble(), 0.0)
                Direction.SOUTH -> Pair(random.nextDouble(), 1.0)
                Direction.EAST -> Pair(1.0, random.nextDouble())
                Direction.WEST -> Pair(0.0, random.nextDouble())
                else -> Pair(random.nextDouble(), random.nextDouble())
            }

        val y = baseY + random.nextDouble() * heightRange

        return Triple(x, y, z)
    }
}
