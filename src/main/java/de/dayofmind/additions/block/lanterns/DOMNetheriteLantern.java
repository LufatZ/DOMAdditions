package de.dayofmind.additions.block.lanterns;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DOMNetheriteLantern extends DOMLanterns {

    protected static final VoxelShape NETHERITE_STANDING_SHAPE;
    protected static final VoxelShape NETHERITE_HANGING_SHAPE;

    public DOMNetheriteLantern(Settings settings) {
        super(settings);
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
    boolean hanging = state.get(HANGING);
    return hanging ? NETHERITE_HANGING_SHAPE : NETHERITE_STANDING_SHAPE;
    }

    static {
        NETHERITE_HANGING_SHAPE = VoxelShapes.union(
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
        NETHERITE_STANDING_SHAPE = VoxelShapes.union(
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
    }
}
