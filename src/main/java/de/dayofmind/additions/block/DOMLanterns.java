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
    protected static final VoxelShape COPPER_STANDING_SHAPE = VoxelShapes.union(
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
    protected static final VoxelShape COPPER_HANGING_SHAPE = VoxelShapes.union(
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
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if(state.isOf(DOMBlocks.NETHERITE_LANTERN)){
            return (Boolean)state.get(HANGING) ? NETHERITE_HANGING_SHAPE : NETHERITE_STANDING_SHAPE;
        }
        if(state.isOf(DOMBlocks.COPPER_LANTERN)){
            return (Boolean)state.get(HANGING) ? COPPER_HANGING_SHAPE : COPPER_STANDING_SHAPE;
        }
        else return (Boolean)state.get(HANGING) ? HANGING_SHAPE : STANDING_SHAPE;
    }
}
