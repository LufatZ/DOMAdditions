package de.additions.blocks

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

/**
 * Represents a slab block variant of Crying Obsidian.
 * This slab emits obsidian tear particles from its edges, with behavior dependent on the slab type.
 *
 * @param settings The settings for the block.
 */
class CryingObsidianSlab(
    settings: Properties,
) : SlabBlock(settings) {
    /**
     * Called periodically to display random particles.
     * There is a 20% chance each tick for particles to spawn.
     * The particle emission logic is delegated based on the slab type.
     */
    override fun animateTick(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        random: RandomSource,
    ) {
        if (random.nextInt(5) == 0) {
            when (state.getValue(TYPE)) {
                SlabType.DOUBLE -> handleDoubleSlab(world, pos, random)
                SlabType.BOTTOM -> handleBottomSlab(world, pos, random)
                SlabType.TOP -> handleTopSlab(world, pos, random)
            }
        }
    }

    /**
     * Handles particle emission for double slabs.
     * Particles are emitted from the center seam in all directions except up.
     */
    private fun handleDoubleSlab(
        world: Level,
        pos: BlockPos,
        random: RandomSource,
    ) {
        val direction = Direction.getRandom(random)
        if (direction != Direction.UP) {
            trySpawnParticle(world, pos, direction, random, 0.5, 0.5)
        }
    }

    /**
     * Handles particle emission for bottom slabs.
     * Particles are emitted from the lower half of the block in all directions except up.
     */
    private fun handleBottomSlab(
        world: Level,
        pos: BlockPos,
        random: RandomSource,
    ) {
        val direction = Direction.getRandom(random)
        if (direction != Direction.UP) {
            trySpawnParticle(world, pos, direction, random, 0.0, 0.5)
        }
    }

    /**
     * Handles particle emission for top slabs.
     * Particles are emitted from the upper half of the block in all directions except down.
     */
    private fun handleTopSlab(
        world: Level,
        pos: BlockPos,
        random: RandomSource,
    ) {
        val direction = Direction.getRandom(random)
        if (direction != Direction.DOWN) {
            trySpawnParticle(world, pos, direction, random, 0.5, 1.0)
        }
    }

    /**
     * Attempts to spawn a particle if the adjacent block side is not opaque.
     *
     * @param yBase The minimum Y position for the particle.
     * @param yRange The maximum Y position for the particle.
     */
    private fun trySpawnParticle(
        world: Level,
        pos: BlockPos,
        direction: Direction,
        random: RandomSource,
        yBase: Double,
        yRange: Double,
    ) {
        val blockPos = pos.relative(direction)
        val blockState = world.getBlockState(blockPos)
        if (!blockState.canOcclude() || !blockState.isFaceSturdy(world, blockPos, direction.opposite)) {
            val (x, y, z) = calculateParticlePosition(direction, random, yBase, yRange)
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
     * Calculates the precise position of a particle along the slab's edges.
     *
     * @param direction The direction of particle emission.
     * @param yBase The base Y position (e.g., 0.0 for bottom, 0.5 for top).
     * @param yRange The Y position range.
     * @return A [Triple] containing the X, Y, and Z offsets within the block space.
     */
    private fun calculateParticlePosition(
        direction: Direction,
        random: RandomSource,
        yBase: Double,
        yRange: Double,
    ): Triple<Double, Double, Double> {
        val x =
            when (direction.axis) {
                Direction.Axis.X -> 0.5 + direction.stepX * 0.6
                else -> random.nextDouble()
            }

        val z =
            when (direction.axis) {
                Direction.Axis.Z -> 0.5 + direction.stepZ * 0.6
                else -> random.nextDouble()
            }

        val y =
            when {
                direction == Direction.DOWN && yBase == 0.0 -> 0.0
                direction == Direction.UP && yRange == 1.0 -> 1.0
                else -> yBase + random.nextDouble() * (yRange - yBase)
            }

        return Triple(x, y, z)
    }
}
