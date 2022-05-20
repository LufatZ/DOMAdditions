package de.dayofmind.additions.block;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DOMLanterns extends LanternBlock implements Waterloggable {
    public DOMLanterns(Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HANGING, false)).with(WATERLOGGED, false));
    }

    protected static final VoxelShape NETHERITE_STANDING_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(5, 11, 5, 11, 12, 11),
            Block.createCuboidShape(4, 10, 4, 12, 11, 12),
            Block.createCuboidShape(3, 8, 3, 13, 10, 13),
            Block.createCuboidShape(5, 2, 5, 11, 8, 11),
            Block.createCuboidShape(3, 2, 12, 4, 8, 13),
            Block.createCuboidShape(3, 2, 3, 4, 8, 4),
            Block.createCuboidShape(12, 2, 3, 13, 8, 4),
            Block.createCuboidShape(12, 2, 12, 13, 8, 13),
            Block.createCuboidShape(3, 1, 3, 13, 2, 13),
            Block.createCuboidShape(4, 0, 4, 12, 1, 12)
    );
    protected static final VoxelShape NETHERITE_HANGING_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(5, 11, 5, 11, 12, 11),
            Block.createCuboidShape(4, 10, 4, 12, 11, 12),
            Block.createCuboidShape(3, 8, 3, 13, 10, 13),
            Block.createCuboidShape(5, 2, 5, 11, 8, 11),
            Block.createCuboidShape(3, 2, 12, 4, 8, 13),
            Block.createCuboidShape(3, 2, 3, 4, 8, 4),
            Block.createCuboidShape(12, 2, 3, 13, 8, 4),
            Block.createCuboidShape(12, 2, 12, 13, 8, 13),
            Block.createCuboidShape(3, 1, 3, 13, 2, 13),
            Block.createCuboidShape(4, 0, 4, 12, 1, 12)
    );
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if(state.isOf(DOMBlocks.NETHERITE_LANTERN)){
            return (Boolean)state.get(HANGING) ? NETHERITE_HANGING_SHAPE : NETHERITE_STANDING_SHAPE;
        }
        else return (Boolean)state.get(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
    }
}
