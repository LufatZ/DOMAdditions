package de.dayofmind.additions.block.stairs;

import de.dayofmind.additions.block.DOMBlocksRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class DOMDirtPathStair extends DOMShortStairs{
    public DOMDirtPathStair(BlockState baseBlockState, Settings settings) {
        super(baseBlockState, settings);
    }
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP) {
            world.scheduleBlockTick(pos, this, 1);
        }

        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, DOMBlocksRegistry.DIRT_STAIR.getDefaultState().with(FACING, state.get(FACING)).with(HALF, state.get(HALF)).with(SHAPE, state.get(SHAPE)).with(WATERLOGGED, state.get(WATERLOGGED)));
    }
}
