package de.dayofmind.additions.block.lanterns;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DOMCopperLantern extends DOMLanterns {

    protected static final VoxelShape COPPER_STANDING_SHAPE;
    protected static final VoxelShape COPPER_HANGING_SHAPE;

    public DOMCopperLantern(Settings settings) {
        super(settings);
    }

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Boolean hanging = state.get(HANGING);
        return hanging ? COPPER_HANGING_SHAPE : COPPER_STANDING_SHAPE;
    }

    static {
        COPPER_HANGING_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(7, 3, 7, 9, 6, 9),
                Block.createCuboidShape(7, 2, 7, 9, 3, 9),
                Block.createCuboidShape(7, 9, 7, 9, 10, 9),
                Block.createCuboidShape(6, 0, 6, 10, 2, 10),
                Block.createCuboidShape(7, 6, 7, 9, 8, 9),
                Block.createCuboidShape(8, 2, 6, 9, 3, 7),
                Block.createCuboidShape(7, 2, 9, 8, 3, 10),
                Block.createCuboidShape(7, 6, 9, 8, 7, 10),
                Block.createCuboidShape(8, 6, 6, 9, 7, 7),
                Block.createCuboidShape(8, 3, 5, 9, 6, 6),
                Block.createCuboidShape(7, 3, 10, 8, 6, 11),
                Block.createCuboidShape(7.5, 8, 7.5, 8.5, 9, 8.5)
        );
        COPPER_STANDING_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(7, 3, 7, 9, 6, 9),
                Block.createCuboidShape(7, 2, 7, 9, 3, 9),
                Block.createCuboidShape(7, 9, 7, 9, 10, 9),
                Block.createCuboidShape(6, 0, 6, 10, 2, 10),
                Block.createCuboidShape(7, 6, 7, 9, 8, 9),
                Block.createCuboidShape(8, 2, 6, 9, 3, 7),
                Block.createCuboidShape(7, 2, 9, 8, 3, 10),
                Block.createCuboidShape(7, 6, 9, 8, 7, 10),
                Block.createCuboidShape(8, 6, 6, 9, 7, 7),
                Block.createCuboidShape(8, 3, 5, 9, 6, 6),
                Block.createCuboidShape(7, 3, 10, 8, 6, 11),
                Block.createCuboidShape(7.5, 8, 7.5, 8.5, 9, 8.5)
        );
    }
}
