package de.additions.blocks

import net.minecraft.block.AbstractBlock.Settings
import net.minecraft.block.BlockState
import net.minecraft.block.SlabBlock
import net.minecraft.block.enums.SlabType
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

/**
 * # Crying Obsidian Slab Block
 *
 * A custom slab block variant that emits obsidian tear particles from its edges.
 * Handles three slab types (bottom, top, double) with different emission rules.
 *
 * ## Particle Behavior:
 * - Double slabs: Particles in all directions except UP
 * - Bottom slabs: Particles below block (Y: 0.0-0.5)
 * - Top slabs: Particles above block (Y: 0.5-1.0)
 *
 * @property settings Block properties (hardness, sounds, etc)
 */
class CryingObsidianSlab(settings: Settings) : SlabBlock(settings) {

    /**
     * Handles visual effects with 20% chance per tick.
     * Delegates to type-specific handlers based on slab configuration.
     *
     * @param state Current block state containing slab type
     * @param world The world instance
     * @param pos Block position in world
     * @param random Random number generator for particle placement
     */
    override fun randomDisplayTick(state: BlockState, world: World, pos: BlockPos, random: Random) {
        if (random.nextInt(5) == 0) {
            when (state.get(TYPE)) {
                SlabType.DOUBLE -> handleDoubleSlab(world, pos, random)
                SlabType.BOTTOM -> handleBottomSlab(world, pos, random)
                SlabType.TOP -> handleTopSlab(world, pos, random)
            }
        }
    }

    /**
     * Handles particle emission for double slabs.
     *
     * @param world World instance
     * @param pos Block position
     * @param random Random number generator
     */
    private fun handleDoubleSlab(world: World, pos: BlockPos, random: Random) {
        val direction = Direction.random(random)
        if (direction != Direction.UP) {
            trySpawnParticle(world, pos, direction, random, 0.5, 0.5)
        }
    }

    /**
     * Handles particle emission for bottom slabs.
     *
     * @param world World instance
     * @param pos Block position
     * @param random Random number generator
     */
    private fun handleBottomSlab(world: World, pos: BlockPos, random: Random) {
        val direction = Direction.random(random)
        if (direction != Direction.UP) {
            trySpawnParticle(world, pos, direction, random, 0.0, 0.5)
        }
    }

    /**
     * Handles particle emission for top slabs.
     *
     * @param world World instance
     * @param pos Block position
     * @param random Random number generator
     */
    private fun handleTopSlab(world: World, pos: BlockPos, random: Random) {
        val direction = Direction.random(random)
        if (direction != Direction.DOWN) {
            trySpawnParticle(world, pos, direction, random, 0.5, 1.0)
        }
    }

    /**
     * Attempts particle spawn after checking adjacent block occlusion.
     *
     * @param world World instance
     * @param pos Base block position
     * @param direction Emission direction
     * @param random Random number generator
     * @param yBase Minimum Y position (0.0-1.0)
     * @param yRange Maximum Y position (must be >= yBase)
     */
    private fun trySpawnParticle(
        world: World,
        pos: BlockPos,
        direction: Direction,
        random: Random,
        yBase: Double,
        yRange: Double
    ) {
        val blockPos = pos.offset(direction)
        val blockState = world.getBlockState(blockPos)
        if (!blockState.isOpaque || !blockState.isSideSolidFullSquare(world, blockPos, direction.opposite)) {
            val (x, y, z) = calculateParticlePosition(direction, random, yBase, yRange)
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
     * Calculates particle positions along slab edges
     *
     * @param direction The emission direction
     * @param random Random number generator
     * @param yBase Base Y position (0.0 for bottom, 0.5 for top)
     * @param yRange Y position range (0.5 for bottom, 0.5 for top)
     * @return Triple of X/Y/Z offsets within block space
     */
    private fun calculateParticlePosition(
        direction: Direction,
        random: Random,
        yBase: Double,
        yRange: Double
    ): Triple<Double, Double, Double> {
        val x = when (direction.axis) {
            Direction.Axis.X -> 0.5 + direction.offsetX * 0.6
            else -> random.nextDouble()
        }

        val z = when (direction.axis) {
            Direction.Axis.Z -> 0.5 + direction.offsetZ * 0.6
            else -> random.nextDouble()
        }

        val y = when {
            direction == Direction.DOWN && yBase == 0.0 -> 0.0
            direction == Direction.UP && yRange == 1.0 -> 1.0
            else -> yBase + random.nextDouble() * (yRange - yBase)
        }

        return Triple(x, y, z)
    }
}