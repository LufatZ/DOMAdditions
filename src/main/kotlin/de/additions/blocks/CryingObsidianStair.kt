package de.additions.blocks

import net.minecraft.block.*
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.StairShape
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

/**
 * # Crying Obsidian Stair Block
 *
 * A custom stair block that emits dripping obsidian tear particles from its edges, similar to crying obsidian.
 * Particles are spawned on the sides depending on the stair's shape and orientation.
 *
 * ## Key Features:
 * - Inherits properties from Minecraft's StairsBlock
 * - Client-side particle effects using `DRIPPING_OBSIDIAN_TEAR`
 * - Smart particle positioning based on stair geometry
 * - Prevents particles from spawning inside solid blocks
 *
 * @property baseBlockState The base block state this stair is modeled after
 * @property settings Block properties (hardness, sounds, etc)
 */
class CryingObsidianStair(baseBlockState: BlockState, settings: Settings) : StairsBlock(baseBlockState, settings) {

    /**
     * Handles per-tick visual effects (client-side only).
     * Attempts to spawn particles with 20% chance each tick (1/5 probability).
     *
     * @param state Current block state containing stair properties
     * @param world The world instance
     * @param pos Block position in world
     * @param random Random number generator for particle placement
     */
    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
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
     * Determines valid particle emission directions based on stair configuration
     *
     * @param facing The primary facing direction of the stair
     * @param half Whether the stair is in upper/lower position
     * @param shape The stair's geometric shape (straight/inner/outer)
     * @return List of valid emission directions
     */
    private fun getValidDirections(facing: Direction, half: BlockHalf, shape: StairShape): List<Direction> {
        return when (half) {
            BlockHalf.BOTTOM -> getBottomValidDirections(facing, shape)
            BlockHalf.TOP -> getTopValidDirections(facing, shape)
        }
    }

    /**
     * Calculates emission directions for bottom-half stairs.
     *
     * @param facing Primary orientation direction
     * @param shape Stair geometric configuration
     * @return List containing:
     *         - Always DOWN direction
     *         - Shape-dependent horizontal directions
     */
    private fun getBottomValidDirections(facing: Direction, shape: StairShape): List<Direction> {
        val directions = mutableListOf(Direction.DOWN)
        when (shape) {
            StairShape.STRAIGHT -> directions.add(facing.opposite)
            StairShape.INNER_LEFT,
            StairShape.OUTER_LEFT -> {
                directions.add(facing.rotateYCounterclockwise())
                directions.add(facing.opposite)
            }
            StairShape.INNER_RIGHT,
            StairShape.OUTER_RIGHT -> {
                directions.add(facing.rotateYClockwise())
                directions.add(facing.opposite)
            }
        }
        return directions
    }

    /**
     * Calculates emission directions for top-half stairs.
     *
     * @param facing Primary orientation direction
     * @param shape Stair geometric configuration
     * @return List containing:
     *         - Always UP direction
     *         - Shape-dependent horizontal directions
     */
    private fun getTopValidDirections(facing: Direction, shape: StairShape): List<Direction> {
        val directions = mutableListOf(Direction.UP)
        when (shape) {
            StairShape.STRAIGHT -> directions.add(facing)
            StairShape.INNER_LEFT,
            StairShape.OUTER_LEFT -> {
                directions.add(facing)
                directions.add(facing.rotateYCounterclockwise())
            }
            StairShape.INNER_RIGHT,
            StairShape.OUTER_RIGHT -> {
                directions.add(facing)
                directions.add(facing.rotateYClockwise())
            }
        }
        return directions
    }

    /**
     * Attempts to spawn a particle in a specific direction after checking block occlusion.
     *
     * @param world The world instance
     * @param pos Base block position
     * @param direction Chosen emission direction
     * @param random Random number generator
     * @param half Stair half (top/bottom) for vertical positioning
     */
    private fun trySpawnParticle(
        world: World,
        pos: BlockPos,
        direction: Direction,
        random: Random,
        half: BlockHalf
    ) {
        val blockPos = pos.offset(direction)
        val blockState = world.getBlockState(blockPos)

        if (!blockState.isOpaque || !blockState.isSideSolidFullSquare(world, blockPos, direction.opposite)) {
            val (x, y, z) = calculateParticlePosition(direction, random, half)
            world.addParticle(
                ParticleTypes.DRIPPING_OBSIDIAN_TEAR,
                pos.x + x,
                pos.y + y,
                pos.z + z,
                0.0,
                0.0,
                0.0
            )
        }
    }

    /**
     * Calculates precise particle position within block boundaries.
     *
     * @param direction Emission direction
     * @param random Random number generator
     * @param half Stair half for vertical positioning
     * @return Triple of X/Y/Z offsets (0.0-1.0 range)
     */
    private fun calculateParticlePosition(
        direction: Direction,
        random: Random,
        half: BlockHalf
    ): Triple<Double, Double, Double> {
        val (baseY, heightRange) = when (half) {
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
     *
     * @param direction Vertical direction (UP/DOWN)
     * @param random Random number generator
     * @param half Stair half for position adjustment
     * @return Position triple with constrained Y-values
     */
    private fun handleVerticalDirection(
        direction: Direction,
        random: Random,
        half: BlockHalf
    ): Triple<Double, Double, Double> {
        val y = when {
            half == BlockHalf.BOTTOM && direction == Direction.DOWN -> 0.1 + random.nextDouble() * 0.4
            half == BlockHalf.TOP && direction == Direction.UP -> 0.9 + random.nextDouble() * 0.1
            else -> random.nextDouble()
        }

        return Triple(
            random.nextDouble(),
            y,
            random.nextDouble()
        )
    }

    /**
     * Handles horizontal particle positioning.
     *
     * @param direction Horizontal emission direction
     * @param random Random number generator
     * @param baseY Base Y-position based on stair half
     * @param heightRange Vertical spread range
     * @return Position triple with edge-aligned coordinates
     */
    private fun handleHorizontalDirection(
        direction: Direction,
        random: Random,
        baseY: Double,
        heightRange: Double
    ): Triple<Double, Double, Double> {
        val (x, z) = when (direction) {
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