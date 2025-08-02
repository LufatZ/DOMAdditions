package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.BubbleColumnBlock
import net.minecraft.block.SlabBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.WorldView
import net.minecraft.world.tick.ScheduledTickView

/**
 * Represents a slab block made of magma.
 * It damages entities that step on it and creates bubble columns when interacting with water.
 *
 * @param settings The settings for the block.
 */
class MagmaSlab (settings: Settings) : SlabBlock(settings) {
    /**
     * Called when an entity steps on the block.
     * Damages living entities that are not immune to stepping effects.
     */
    override fun onSteppedOn(world: World, pos: BlockPos, state: BlockState, entity: Entity) {
        if (!entity.bypassesSteppingEffects() && entity is LivingEntity && world is ServerWorld) {
            entity.damage(
                world,
                world.damageSources.hotFloor(),
                1.0f
            )
        }
        super.onSteppedOn(world, pos, state, entity)
    }

    /**
     * Called when a scheduled tick occurs for the block.
     * Updates bubble columns in the block above.
     */
    override fun scheduledTick(state: BlockState, world: ServerWorld, pos: BlockPos, random: Random) {
        BubbleColumnBlock.update(world, pos.up(), state)
    }

    /**
     * Called when a neighboring block is updated.
     * Schedules a block tick if water is placed on top of this block.
     */
    override fun getStateForNeighborUpdate(
        state: BlockState,
        world: WorldView,
        tickView: ScheduledTickView,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: Random
    ): BlockState {
        if (direction == Direction.UP && neighborState.isOf(Blocks.WATER)) {
            tickView.scheduleBlockTick(pos, this, 20)
        }
        return super.getStateForNeighborUpdate(state, world, tickView,pos, direction, neighborPos, neighborState, random)
    }

    /**
     * Called when the block is added to the world.
     * Schedules a block tick to handle initial interactions.
     */
    override fun onBlockAdded(state: BlockState, world: World, pos: BlockPos, oldState: BlockState, notify: Boolean) {
        world.scheduleBlockTick(pos, this, 20)
    }
}