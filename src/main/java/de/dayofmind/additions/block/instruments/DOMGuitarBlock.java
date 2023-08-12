package de.dayofmind.additions.block.instruments;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DOMGuitarBlock extends DOMInstrumentBlock {

    private static final VoxelShape STANDING_NORTH_SHAPE;
    private static final VoxelShape STANDING_EAST_SHAPE;
    private static final VoxelShape STANDING_WEST_SHAPE;
    private static final VoxelShape STANDING_SOUTH_SHAPE;

    public DOMGuitarBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(INSTRUMENT, Instrument.GUITAR));

    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);
        return switch (dir) {
            default -> STANDING_NORTH_SHAPE;
            case SOUTH -> STANDING_SOUTH_SHAPE;
            case EAST -> STANDING_EAST_SHAPE;
            case WEST -> STANDING_WEST_SHAPE;
        };
    }

    static {
        STANDING_NORTH_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(4, 2, 7, 5, 5, 10),
                Block.createCuboidShape(8, 2, 6.5, 8, 19, 7),
                Block.createCuboidShape(9, 2, 6.5, 9, 18, 7),
                Block.createCuboidShape(7, 2, 6.5, 7, 18, 7),
                Block.createCuboidShape(9, 16, 7, 10, 19, 8),
                Block.createCuboidShape(6, 16, 7, 7, 19, 8),
                Block.createCuboidShape(7, 13, 7, 9, 16, 8),
                Block.createCuboidShape(7, 7, 7, 9, 13, 9),
                Block.createCuboidShape(7, 17, 7, 9, 18, 8),
                Block.createCuboidShape(7, 19, 7, 9, 20, 8),
                Block.createCuboidShape(7, 0, 7, 9, 4, 9),
                Block.createCuboidShape(11, 6, 7, 12, 8, 10),
                Block.createCuboidShape(4, 6, 7, 5, 8, 10),
                Block.createCuboidShape(11, 2, 7, 12, 5, 10),
                Block.createCuboidShape(7, 0, 9, 9, 11, 10),
                Block.createCuboidShape(6, 0, 7, 7, 10, 10),
                Block.createCuboidShape(9, 0, 7, 10, 10, 10),
                Block.createCuboidShape(5, 1, 7, 6, 9, 10),
                Block.createCuboidShape(10, 1, 7, 11, 9, 10),
                Block.createCuboidShape(6.5, 1, 6, 9.5, 2, 7)
        );
        STANDING_EAST_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(6, 2, 4, 9, 5, 5),
                Block.createCuboidShape(9, 2, 8, 9.5, 19, 8),
                Block.createCuboidShape(9, 2, 9, 9.5, 18, 9),
                Block.createCuboidShape(9, 2, 7, 9.5, 18, 7),
                Block.createCuboidShape(8, 16, 9, 9, 19, 10),
                Block.createCuboidShape(8, 16, 6, 9, 19, 7),
                Block.createCuboidShape(8, 13, 7, 9, 16, 9),
                Block.createCuboidShape(7, 7, 7, 9, 13, 9),
                Block.createCuboidShape(8, 17, 7, 9, 18, 9),
                Block.createCuboidShape(8, 19, 7, 9, 20, 9),
                Block.createCuboidShape(7, 0, 7, 9, 4, 9),
                Block.createCuboidShape(6, 6, 11, 9, 8, 12),
                Block.createCuboidShape(6, 6, 4, 9, 8, 5),
                Block.createCuboidShape(6, 2, 11, 9, 5, 12),
                Block.createCuboidShape(6, 0, 7, 7, 11, 9),
                Block.createCuboidShape(6, 0, 6, 9, 10, 7),
                Block.createCuboidShape(6, 0, 9, 9, 10, 10),
                Block.createCuboidShape(6, 1, 5, 9, 9, 6),
                Block.createCuboidShape(6, 1, 10, 9, 9, 11),
                Block.createCuboidShape(9, 1, 6.5, 10, 2, 9.5)
        );
        STANDING_SOUTH_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(11, 2, 6, 12, 5, 9),
                Block.createCuboidShape(8, 2, 9, 8, 19, 9.5),
                Block.createCuboidShape(7, 2, 9, 7, 18, 9.5),
                Block.createCuboidShape(9, 2, 9, 9, 18, 9.5),
                Block.createCuboidShape(6, 16, 8, 7, 19, 9),
                Block.createCuboidShape(9, 16, 8, 10, 19, 9),
                Block.createCuboidShape(7, 13, 8, 9, 16, 9),
                Block.createCuboidShape(7, 7, 7, 9, 13, 9),
                Block.createCuboidShape(7, 17, 8, 9, 18, 9),
                Block.createCuboidShape(7, 19, 8, 9, 20, 9),
                Block.createCuboidShape(7, 0, 7, 9, 4, 9),
                Block.createCuboidShape(4, 6, 6, 5, 8, 9),
                Block.createCuboidShape(11, 6, 6, 12, 8, 9),
                Block.createCuboidShape(4, 2, 6, 5, 5, 9),
                Block.createCuboidShape(7, 0, 6, 9, 11, 7),
                Block.createCuboidShape(9, 0, 6, 10, 10, 9),
                Block.createCuboidShape(6, 0, 6, 7, 10, 9),
                Block.createCuboidShape(10, 1, 6, 11, 9, 9),
                Block.createCuboidShape(5, 1, 6, 6, 9, 9),
                Block.createCuboidShape(6.5, 1, 9, 9.5, 2, 10)
        );
        STANDING_WEST_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(7, 2, 11, 10, 5, 12),
                Block.createCuboidShape(6.5, 2, 8, 7, 19, 8),
                Block.createCuboidShape(6.5, 2, 7, 7, 18, 7),
                Block.createCuboidShape(6.5, 2, 9, 7, 18, 9),
                Block.createCuboidShape(7, 16, 6, 8, 19, 7),
                Block.createCuboidShape(7, 16, 9, 8, 19, 10),
                Block.createCuboidShape(7, 13, 7, 8, 16, 9),
                Block.createCuboidShape(7, 7, 7, 9, 13, 9),
                Block.createCuboidShape(7, 17, 7, 8, 18, 9),
                Block.createCuboidShape(7, 19, 7, 8, 20, 9),
                Block.createCuboidShape(7, 0, 7, 9, 4, 9),
                Block.createCuboidShape(7, 6, 4, 10, 8, 5),
                Block.createCuboidShape(7, 6, 11, 10, 8, 12),
                Block.createCuboidShape(7, 2, 4, 10, 5, 5),
                Block.createCuboidShape(9, 0, 7, 10, 11, 9),
                Block.createCuboidShape(7, 0, 9, 10, 10, 10),
                Block.createCuboidShape(7, 0, 6, 10, 10, 7),
                Block.createCuboidShape(7, 1, 10, 10, 9, 11),
                Block.createCuboidShape(7, 1, 5, 10, 9, 6),
                Block.createCuboidShape(6, 1, 6.5, 7, 2, 9.5)
        );
    }

}
