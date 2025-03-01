package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.block.BubbleColumnBlock
import net.minecraft.block.StairsBlock
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.WorldView
import net.minecraft.world.tick.ScheduledTickView

class MagmaStair(
    blockstate: BlockState,
    settings: Settings,
) : StairsBlock(blockstate, settings) {
    override fun onSteppedOn(
        world: World,
        pos: BlockPos,
        state: BlockState,
        entity: Entity,
    ) {
        if (!entity.bypassesSteppingEffects() && entity is LivingEntity && world is ServerWorld) {
            entity.damage(
                world,
                world.damageSources.hotFloor(),
                1.0f,
            )
        }
        super.onSteppedOn(world, pos, state, entity)
    }

    override fun scheduledTick(
        state: BlockState,
        world: ServerWorld,
        pos: BlockPos,
        random: Random,
    ) {
        BubbleColumnBlock.update(world, pos.up(), state)
    }

    override fun getStateForNeighborUpdate(
        state: BlockState,
        world: WorldView,
        tickView: ScheduledTickView,
        pos: BlockPos,
        direction: Direction,
        neighborPos: BlockPos,
        neighborState: BlockState,
        random: Random,
    ): BlockState {
        if (direction == Direction.UP && neighborState.isOf(Blocks.WATER)) {
            tickView.scheduleBlockTick(pos, this, 20)
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random)
    }

    override fun onBlockAdded(
        state: BlockState,
        world: World,
        pos: BlockPos,
        oldState: BlockState,
        notify: Boolean,
    ) {
        world.scheduleBlockTick(pos, this, 20)
    }
}
