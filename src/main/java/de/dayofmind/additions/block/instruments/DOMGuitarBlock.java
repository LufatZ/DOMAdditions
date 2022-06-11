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
        this.setDefaultState(this.stateManager.getDefaultState().with(INSTRUMENT, Instrument.GUITAR).with(NOTE, 0).with(POWERED, false).with(FACING, Direction.NORTH).with(WATERLOGGED, false));

    }
    //TODO don´t know why, but maybe it is the lightning on the top sided textures that looks wierd (if direction isn´t north)

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
                Block.createCuboidShape(8, 2, 6.5, 8, 19, 7),
                Block.createCuboidShape(9, 2, 6.5, 9, 18, 7),
                Block.createCuboidShape(7, 2, 6.5, 7, 18, 7),
                Block.createCuboidShape(9.5, 16, 7, 10.5, 19, 8),
                Block.createCuboidShape(5.5, 16, 7, 6.5, 19, 8),
                Block.createCuboidShape(6.5, 13, 7, 9.5, 16, 8),
                Block.createCuboidShape(6.5, 11, 7, 9.5, 13, 9),
                Block.createCuboidShape(7.5, 16, 7, 8.5, 19, 8),
                Block.createCuboidShape(6.5, 17, 7, 7.5, 18, 8),
                Block.createCuboidShape(8.5, 17, 7, 9.5, 18, 8),
                Block.createCuboidShape(6.5, 19, 7, 9.5, 20, 8),
                Block.createCuboidShape(7.5, 7, 7, 8.5, 11, 8),
                Block.createCuboidShape(7.5, 0, 7, 8.5, 3, 8),
                Block.createCuboidShape(8.5, 6, 7, 9.5, 11, 8),
                Block.createCuboidShape(6.5, 6, 7, 7.5, 11, 8),
                Block.createCuboidShape(6.5, 0, 7, 7.5, 4, 8),
                Block.createCuboidShape(8.5, 0, 7, 9.5, 4, 8),
                Block.createCuboidShape(11.5, 6, 7, 12.5, 8, 10),
                Block.createCuboidShape(3.5, 6, 7, 4.5, 8, 10),
                Block.createCuboidShape(11.5, 2, 7, 12.5, 5, 10),
                Block.createCuboidShape(3.5, 2, 7, 4.5, 5, 10),
                Block.createCuboidShape(6.5, 0, 8, 9.5, 11, 10),
                Block.createCuboidShape(5.5, 0, 7, 6.5, 10, 10),
                Block.createCuboidShape(9.5, 0, 7, 10.5, 10, 10),
                Block.createCuboidShape(4.5, 1, 7, 5.5, 9, 10),
                Block.createCuboidShape(10.5, 1, 7, 11.5, 9, 10),
                Block.createCuboidShape(6.5, 1, 6, 9.5, 2, 7)
        );
        STANDING_EAST_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(9, 2, 8, 9.5, 19, 8),
                Block.createCuboidShape(9, 2, 9, 9.5, 18, 9),
                Block.createCuboidShape(9, 2, 7, 9.5, 18, 7),
                Block.createCuboidShape(8, 16, 9.5, 9, 19, 10.5),
                Block.createCuboidShape(8, 16, 5.5, 9, 19, 6.5),
                Block.createCuboidShape(8, 13, 6.5, 9, 16, 9.5),
                Block.createCuboidShape(7, 11, 6.5, 9, 13, 9.5),
                Block.createCuboidShape(8, 16, 7.5, 9, 19, 8.5),
                Block.createCuboidShape(8, 17, 6.5, 9, 18, 7.5),
                Block.createCuboidShape(8, 17, 8.5, 9, 18, 9.5),
                Block.createCuboidShape(8, 19, 6.5, 9, 20, 9.5),
                Block.createCuboidShape(8, 7, 7.5, 9, 11, 8.5),
                Block.createCuboidShape(8, 0, 7.5, 9, 3, 8.5),
                Block.createCuboidShape(8, 6, 8.5, 9, 11, 9.5),
                Block.createCuboidShape(8, 6, 6.5, 9, 11, 7.5),
                Block.createCuboidShape(8, 0, 6.5, 9, 4, 7.5),
                Block.createCuboidShape(8, 0, 8.5, 9, 4, 9.5),
                Block.createCuboidShape(6, 6, 11.5, 9, 8, 12.5),
                Block.createCuboidShape(6, 6, 3.5, 9, 8, 4.5),
                Block.createCuboidShape(6, 2, 11.5, 9, 5, 12.5),
                Block.createCuboidShape(6, 2, 3.5, 9, 5, 4.5),
                Block.createCuboidShape(6, 0, 6.5, 8, 11, 9.5),
                Block.createCuboidShape(6, 0, 5.5, 9, 10, 6.5),
                Block.createCuboidShape(6, 0, 9.5, 9, 10, 10.5),
                Block.createCuboidShape(6, 1, 4.5, 9, 9, 5.5),
                Block.createCuboidShape(6, 1, 10.5, 9, 9, 11.5),
                Block.createCuboidShape(9, 1, 6.5, 10, 2, 9.5)
        );
        STANDING_SOUTH_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(8, 2, 9, 8, 19, 9.5),
                Block.createCuboidShape(7, 2, 9, 7, 18, 9.5),
                Block.createCuboidShape(9, 2, 9, 9, 18, 9.5),
                Block.createCuboidShape(5.5, 16, 8, 6.5, 19, 9),
                Block.createCuboidShape(9.5, 16, 8, 10.5, 19, 9),
                Block.createCuboidShape(6.5, 13, 8, 9.5, 16, 9),
                Block.createCuboidShape(6.5, 11, 7, 9.5, 13, 9),
                Block.createCuboidShape(7.5, 16, 8, 8.5, 19, 9),
                Block.createCuboidShape(8.5, 17, 8, 9.5, 18, 9),
                Block.createCuboidShape(6.5, 17, 8, 7.5, 18, 9),
                Block.createCuboidShape(6.5, 19, 8, 9.5, 20, 9),
                Block.createCuboidShape(7.5, 7, 8, 8.5, 11, 9),
                Block.createCuboidShape(7.5, 0, 8, 8.5, 3, 9),
                Block.createCuboidShape(6.5, 6, 8, 7.5, 11, 9),
                Block.createCuboidShape(8.5, 6, 8, 9.5, 11, 9),
                Block.createCuboidShape(8.5, 0, 8, 9.5, 4, 9),
                Block.createCuboidShape(6.5, 0, 8, 7.5, 4, 9),
                Block.createCuboidShape(3.5, 6, 6, 4.5, 8, 9),
                Block.createCuboidShape(11.5, 6, 6, 12.5, 8, 9),
                Block.createCuboidShape(3.5, 2, 6, 4.5, 5, 9),
                Block.createCuboidShape(11.5, 2, 6, 12.5, 5, 9),
                Block.createCuboidShape(6.5, 0, 6, 9.5, 11, 8),
                Block.createCuboidShape(9.5, 0, 6, 10.5, 10, 9),
                Block.createCuboidShape(5.5, 0, 6, 6.5, 10, 9),
                Block.createCuboidShape(10.5, 1, 6, 11.5, 9, 9),
                Block.createCuboidShape(4.5, 1, 6, 5.5, 9, 9),
                Block.createCuboidShape(6.5, 1, 9, 9.5, 2, 10)
        );
        STANDING_WEST_SHAPE = VoxelShapes.union(
                Block.createCuboidShape(6.5, 2, 8, 7, 19, 8),
                Block.createCuboidShape(6.5, 2, 7, 7, 18, 7),
                Block.createCuboidShape(6.5, 2, 9, 7, 18, 9),
                Block.createCuboidShape(7, 16, 5.5, 8, 19, 6.5),
                Block.createCuboidShape(7, 16, 9.5, 8, 19, 10.5),
                Block.createCuboidShape(7, 13, 6.5, 8, 16, 9.5),
                Block.createCuboidShape(7, 11, 6.5, 9, 13, 9.5),
                Block.createCuboidShape(7, 16, 7.5, 8, 19, 8.5),
                Block.createCuboidShape(7, 17, 8.5, 8, 18, 9.5),
                Block.createCuboidShape(7, 17, 6.5, 8, 18, 7.5),
                Block.createCuboidShape(7, 19, 6.5, 8, 20, 9.5),
                Block.createCuboidShape(7, 7, 7.5, 8, 11, 8.5),
                Block.createCuboidShape(7, 0, 7.5, 8, 3, 8.5),
                Block.createCuboidShape(7, 6, 6.5, 8, 11, 7.5),
                Block.createCuboidShape(7, 6, 8.5, 8, 11, 9.5),
                Block.createCuboidShape(7, 0, 8.5, 8, 4, 9.5),
                Block.createCuboidShape(7, 0, 6.5, 8, 4, 7.5),
                Block.createCuboidShape(7, 6, 3.5, 10, 8, 4.5),
                Block.createCuboidShape(7, 6, 11.5, 10, 8, 12.5),
                Block.createCuboidShape(7, 2, 3.5, 10, 5, 4.5),
                Block.createCuboidShape(7, 2, 11.5, 10, 5, 12.5),
                Block.createCuboidShape(8, 0, 6.5, 10, 11, 9.5),
                Block.createCuboidShape(7, 0, 9.5, 10, 10, 10.5),
                Block.createCuboidShape(7, 0, 5.5, 10, 10, 6.5),
                Block.createCuboidShape(7, 1, 10.5, 10, 9, 11.5),
                Block.createCuboidShape(7, 1, 4.5, 10, 9, 5.5),
                Block.createCuboidShape(6, 1, 6.5, 7, 2, 9.5)
        );
    }

}
