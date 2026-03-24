package de.additions.blocks

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.BubbleColumnBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.ScheduledTickAccess

/**
 * Represents a stair block made of magma.
 * It damages entities that step on it and creates bubble columns when interacting with water.
 *
 * @param blockstate The base block state of the stair.
 * @param settings The settings for the block.
 */
class MagmaStair(
    blockstate: BlockState,
    settings: Properties,
) : StairBlock(blockstate, settings) {
    /**
     * Called when an entity steps on the block.
     * Damages living entities that are not immune to stepping effects.
     */
    override fun stepOn(
        world: Level,
        pos: BlockPos,
        state: BlockState,
        entity: Entity,
    ) {
        if (!entity.isSteppingCarefully && entity is LivingEntity && world is ServerLevel) {
            entity.hurtServer(
                world,
                world.damageSources().hotFloor(),
                1.0f,
            )
        }
        super.stepOn(world, pos, state, entity)
    }

    /**
     * Called when a scheduled tick occurs for the block.
     * Updates bubble columns in the block above.
     */
    override fun tick(
        state: BlockState,
        world: ServerLevel,
        pos: BlockPos,
        random: RandomSource,
    ) {
        BubbleColumnBlock.updateColumn(world, pos.above(), state)
    }

    /**
     * Called when a neighboring block is updated.
     * Schedules a block tick if water is placed on top of this block.
     */
    override fun updateShape(
        state: BlockState,
        world: LevelReader,
        tickView: ScheduledTickAccess,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: RandomSource,
    ): BlockState {
        if (direction == Direction.UP && neighborState.`is`(Blocks.WATER)) {
            tickView.scheduleTick(pos, this, 20)
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random)
    }

    /**
     * Called when the block is added to the world.
     * Schedules a block tick to handle initial interactions.
     */
    override fun onPlace(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        oldState: BlockState,
        notify: Boolean,
    ) {
        world.scheduleTick(pos, this, 20)
    }
}
