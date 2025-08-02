package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.SlabBlock
import net.minecraft.block.enums.SlabType
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

/**
 * Represents a slab block variant of Crying Obsidian.
 * This slab emits obsidian tear particles from its edges, with behavior dependent on the slab type.
 *
 * @param settings The settings for the block.
 */
class CryingObsidianSlab(
    settings: Settings,
) : SlabBlock(settings) {
    /**
     * Called periodically to display random particles.
     * There is a 20% chance each tick for particles to spawn.
     * The particle emission logic is delegated based on the slab type.
     */
    override fun randomDisplayTick(
        state: BlockState,
        world: World,
        pos: BlockPos,
        random: Random,
    ) {
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
     * Particles are emitted from the center seam in all directions except up.
     */
    private fun handleDoubleSlab(
        world: World,
        pos: BlockPos,
        random: Random,
    ) {
        val direction = Direction.random(random)
        if (direction != Direction.UP) {
            trySpawnParticle(world, pos, direction, random, 0.5, 0.5)
        }
    }

    /**
     * Handles particle emission for bottom slabs.
     * Particles are emitted from the lower half of the block in all directions except up.
     */
    private fun handleBottomSlab(
        world: World,
        pos: BlockPos,
        random: Random,
    ) {
        val direction = Direction.random(random)
        if (direction != Direction.UP) {
            trySpawnParticle(world, pos, direction, random, 0.0, 0.5)
        }
    }

    /**
     * Handles particle emission for top slabs.
     * Particles are emitted from the upper half of the block in all directions except down.
     */
    private fun handleTopSlab(
        world: World,
        pos: BlockPos,
        random: Random,
    ) {
        val direction = Direction.random(random)
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
        world: World,
        pos: BlockPos,
        direction: Direction,
        random: Random,
        yBase: Double,
        yRange: Double,
    ) {
        val blockPos = pos.offset(direction)
        val blockState = world.getBlockState(blockPos)
        if (!blockState.isOpaque || !blockState.isSideSolidFullSquare(world, blockPos, direction.opposite)) {
            val (x, y, z) = calculateParticlePosition(direction, random, yBase, yRange)
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
     * Calculates the precise position of a particle along the slab's edges.
     *
     * @param direction The direction of particle emission.
     * @param yBase The base Y position (e.g., 0.0 for bottom, 0.5 for top).
     * @param yRange The Y position range.
     * @return A [Triple] containing the X, Y, and Z offsets within the block space.
     */
    private fun calculateParticlePosition(
        direction: Direction,
        random: Random,
        yBase: Double,
        yRange: Double,
    ): Triple<Double, Double, Double> {
        val x =
            when (direction.axis) {
                Direction.Axis.X -> 0.5 + direction.offsetX * 0.6
                else -> random.nextDouble()
            }

        val z =
            when (direction.axis) {
                Direction.Axis.Z -> 0.5 + direction.offsetZ * 0.6
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
