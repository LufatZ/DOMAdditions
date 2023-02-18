package de.dayofmind.additions.block.stairs;

import de.dayofmind.additions.block.DOMBlocksRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DOMStairs extends StairsBlock implements Waterloggable {
    public DOMStairs(BlockState baseBlockState, Settings settings) {
        super(baseBlockState, settings);
    }

    public static void setToDirtStair(BlockState state, World world, BlockPos pos) {
        world.setBlockState(pos, DOMBlocksRegistry.DIRT_STAIR.getDefaultState().with(FACING, state.get(FACING)).with(HALF, state.get(HALF)).with(SHAPE, state.get(SHAPE)).with(WATERLOGGED, state.get(WATERLOGGED)));
    }
}
