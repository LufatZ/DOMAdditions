package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.StairsBlock
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.StairShape
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

/**
 * Represents a stair block variant of Crying Obsidian.
 * This stair emits obsidian tear particles from its edges, with behavior dependent on the stair's shape and orientation.
 *
 * @param baseBlockState The base block state this stair is modeled after.
 * @param settings The settings for the block.
 */
class CryingObsidianStair(
    baseBlockState: BlockState,
    settings: Settings,
) : StairsBlock(baseBlockState, settings) {
    /**
     * Called periodically to display random particles.
     * There is a 20% chance each tick for particles to spawn.
     * The particle emission logic is delegated based on the stair's properties.
     */
    override fun randomDisplayTick(
        state: BlockState,
        world: World,
        pos: BlockPos,
        random: Random,
    ) {
        if (random.nextInt(5) == 0) {
            val facing = state.get(FACING)
            val half = state.get(HALF)
            val shape = state.get(SHAPE)

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
        half: BlockHalf,
        shape: StairShape,
    ): List<Direction> =
        when (half) {
            BlockHalf.BOTTOM -> getBottomValidDirections(facing, shape)
            BlockHalf.TOP -> getTopValidDirections(facing, shape)
        }

    /**
     * Calculates the emission directions for a bottom-half stair.
     */
    private fun getBottomValidDirections(
        facing: Direction,
        shape: StairShape,
    ): List<Direction> {
        val directions = mutableListOf(Direction.DOWN)
        when (shape) {
            StairShape.STRAIGHT -> directions.add(facing.opposite)
            StairShape.INNER_LEFT,
            StairShape.OUTER_LEFT,
            -> {
                directions.add(facing.rotateYCounterclockwise())
                directions.add(facing.opposite)
            }
            StairShape.INNER_RIGHT,
            StairShape.OUTER_RIGHT,
            -> {
                directions.add(facing.rotateYClockwise())
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
        shape: StairShape,
    ): List<Direction> {
        val directions = mutableListOf(Direction.UP)
        when (shape) {
            StairShape.STRAIGHT -> directions.add(facing)
            StairShape.INNER_LEFT,
            StairShape.OUTER_LEFT,
            -> {
                directions.add(facing)
                directions.add(facing.rotateYCounterclockwise())
            }
            StairShape.INNER_RIGHT,
            StairShape.OUTER_RIGHT,
            -> {
                directions.add(facing)
                directions.add(facing.rotateYClockwise())
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
        world: World,
        pos: BlockPos,
        direction: Direction,
        random: Random,
        half: BlockHalf,
    ) {
        val blockPos = pos.offset(direction)
        val blockState = world.getBlockState(blockPos)

        if (!blockState.isOpaque || !blockState.isSideSolidFullSquare(world, blockPos, direction.opposite)) {
            val (x, y, z) = calculateParticlePosition(direction, random, half)
            world.addParticleClient(
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
        random: Random,
        half: BlockHalf,
    ): Triple<Double, Double, Double> {
        val (baseY, heightRange) =
            when (half) {
                BlockHalf.BOTTOM -> 0.25 to 0.25
                BlockHalf.TOP -> 0.5 to 0.5
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
        random: Random,
        half: BlockHalf,
    ): Triple<Double, Double, Double> {
        val y =
            when {
                half == BlockHalf.BOTTOM && direction == Direction.DOWN -> 0.1 + random.nextDouble() * 0.4
                half == BlockHalf.TOP && direction == Direction.UP -> 0.9 + random.nextDouble() * 0.1
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
        random: Random,
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
