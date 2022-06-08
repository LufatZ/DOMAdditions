package de.dayofmind.additions.block;

import net.minecraft.block.enums.Instrument;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

public class DOMGuitarBlock extends DOMInstrumentBlock{

    public DOMGuitarBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(INSTRUMENT, Instrument.GUITAR).with(NOTE, 0).with(POWERED, false).with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(WATERLOGGED, false));

    }
    /*
    public VoxelShape STANDING_NORTH_SHAPE(){
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.125, 0.40625, 0.5, 1.1875, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5625, 0.125, 0.40625, 0.5625, 1.125, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.125, 0.40625, 0.4375, 1.125, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.59375, 1, 0.4375, 0.65625, 1.1875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.34375, 1, 0.4375, 0.40625, 1.1875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.8125, 0.4375, 0.59375, 1, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.6875, 0.4375, 0.59375, 0.8125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.46875, 1, 0.4375, 0.53125, 1.1875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 1.0625, 0.4375, 0.46875, 1.125, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.53125, 1.0625, 0.4375, 0.59375, 1.125, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 1.1875, 0.4375, 0.59375, 1.25, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.46875, 0.4375, 0.4375, 0.53125, 0.6875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.46875, 0, 0.4375, 0.53125, 0.1875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.53125, 0.375, 0.4375, 0.59375, 0.6875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.375, 0.4375, 0.46875, 0.6875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0, 0.4375, 0.46875, 0.25, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.53125, 0, 0.4375, 0.59375, 0.25, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.71875, 0.375, 0.4375, 0.78125, 0.5, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.21875, 0.375, 0.4375, 0.28125, 0.5, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.71875, 0.125, 0.4375, 0.78125, 0.3125, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.21875, 0.125, 0.4375, 0.28125, 0.3125, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0, 0.5, 0.59375, 0.6875, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.34375, 0, 0.4375, 0.40625, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.59375, 0, 0.4375, 0.65625, 0.625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.28125, 0.0625, 0.4375, 0.34375, 0.5625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.65625, 0.0625, 0.4375, 0.71875, 0.5625, 0.625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.0625, 0.375, 0.59375, 0.125, 0.4375));

        return shape;
    }
    public VoxelShape STANDING_WEST_SHAPE(){
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.125, 0.5, 0.4375, 1.1875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.125, 0.4375, 0.4375, 1.125, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.125, 0.5625, 0.4375, 1.125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 1, 0.34375, 0.5, 1.1875, 0.40625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 1, 0.59375, 0.5, 1.1875, 0.65625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.8125, 0.40625, 0.5, 1, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.6875, 0.40625, 0.5625, 0.8125, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 1, 0.46875, 0.5, 1.1875, 0.53125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 1.0625, 0.53125, 0.5, 1.125, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 1.0625, 0.40625, 0.5, 1.125, 0.46875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 1.1875, 0.40625, 0.5, 1.25, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.4375, 0.46875, 0.5, 0.6875, 0.53125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0, 0.46875, 0.5, 0.1875, 0.53125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.375, 0.40625, 0.5, 0.6875, 0.46875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.375, 0.53125, 0.5, 0.6875, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0, 0.53125, 0.5, 0.25, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0, 0.40625, 0.5, 0.25, 0.46875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.375, 0.21875, 0.625, 0.5, 0.28125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.375, 0.71875, 0.625, 0.5, 0.78125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.125, 0.21875, 0.625, 0.3125, 0.28125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.125, 0.71875, 0.625, 0.3125, 0.78125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.40625, 0.625, 0.6875, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0, 0.59375, 0.625, 0.625, 0.65625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0, 0.34375, 0.625, 0.625, 0.40625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.0625, 0.65625, 0.625, 0.5625, 0.71875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.0625, 0.28125, 0.625, 0.5625, 0.34375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.0625, 0.40625, 0.4375, 0.125, 0.59375));

        return shape;
    }
    public VoxelShape STANDING_SOUTH_SHAPE(){
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.125, 0.5625, 0.5, 1.1875, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.125, 0.5625, 0.4375, 1.125, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5625, 0.125, 0.5625, 0.5625, 1.125, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.34375, 1, 0.5, 0.40625, 1.1875, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.59375, 1, 0.5, 0.65625, 1.1875, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.8125, 0.5, 0.59375, 1, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.6875, 0.4375, 0.59375, 0.8125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.46875, 1, 0.5, 0.53125, 1.1875, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.53125, 1.0625, 0.5, 0.59375, 1.125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 1.0625, 0.5, 0.46875, 1.125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 1.1875, 0.5, 0.59375, 1.25, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.46875, 0.4375, 0.5, 0.53125, 0.6875, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.46875, 0, 0.5, 0.53125, 0.1875, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.375, 0.5, 0.46875, 0.6875, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.53125, 0.375, 0.5, 0.59375, 0.6875, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.53125, 0, 0.5, 0.59375, 0.25, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0, 0.5, 0.46875, 0.25, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.21875, 0.375, 0.375, 0.28125, 0.5, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.71875, 0.375, 0.375, 0.78125, 0.5, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.21875, 0.125, 0.375, 0.28125, 0.3125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.71875, 0.125, 0.375, 0.78125, 0.3125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0, 0.375, 0.59375, 0.6875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.59375, 0, 0.375, 0.65625, 0.625, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.34375, 0, 0.375, 0.40625, 0.625, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.65625, 0.0625, 0.375, 0.71875, 0.5625, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.28125, 0.0625, 0.375, 0.34375, 0.5625, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.40625, 0.0625, 0.5625, 0.59375, 0.125, 0.625));

        return shape;
    }
    public VoxelShape STANDING_EAST_SHAPE(){
        VoxelShape shape = VoxelShapes.empty();
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5625, 0.125, 0.5, 0.59375, 1.1875, 0.5));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5625, 0.125, 0.5625, 0.59375, 1.125, 0.5625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5625, 0.125, 0.4375, 0.59375, 1.125, 0.4375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 1, 0.59375, 0.5625, 1.1875, 0.65625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 1, 0.34375, 0.5625, 1.1875, 0.40625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.8125, 0.40625, 0.5625, 1, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.4375, 0.6875, 0.40625, 0.5625, 0.8125, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 1, 0.46875, 0.5625, 1.1875, 0.53125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 1.0625, 0.40625, 0.5625, 1.125, 0.46875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 1.0625, 0.53125, 0.5625, 1.125, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 1.1875, 0.40625, 0.5625, 1.25, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.4375, 0.46875, 0.5625, 0.6875, 0.53125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.46875, 0.5625, 0.1875, 0.53125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.375, 0.53125, 0.5625, 0.6875, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0.375, 0.40625, 0.5625, 0.6875, 0.46875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.40625, 0.5625, 0.25, 0.46875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5, 0, 0.53125, 0.5625, 0.25, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.71875, 0.5625, 0.5, 0.78125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.375, 0.21875, 0.5625, 0.5, 0.28125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.125, 0.71875, 0.5625, 0.3125, 0.78125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.125, 0.21875, 0.5625, 0.3125, 0.28125));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0, 0.40625, 0.5, 0.6875, 0.59375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0, 0.34375, 0.5625, 0.625, 0.40625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0, 0.59375, 0.5625, 0.625, 0.65625));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.0625, 0.28125, 0.5625, 0.5625, 0.34375));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.375, 0.0625, 0.65625, 0.5625, 0.5625, 0.71875));
        shape = VoxelShapes.union(shape, VoxelShapes.cuboid(0.5625, 0.0625, 0.40625, 0.625, 0.125, 0.59375));

        return shape;
    }
    public VoxelShape STANDING_SHAPE(){
        return STANDING_NORTH_SHAPE();
    }


    //TODO don´t know why, but maybe it is the lightning on the top sided textures that looks wierd (if direction isn´t north)
    //TODO bad performance with guitar voxelshape (maybe simplify the shape)
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        Direction dir = state.get(FACING);
        return switch (dir) {
            case NORTH -> STANDING_NORTH_SHAPE();
            case SOUTH -> STANDING_SOUTH_SHAPE();
            case EAST -> STANDING_EAST_SHAPE();
            case WEST -> STANDING_WEST_SHAPE();
            default -> STANDING_SHAPE();
        };
    }
    Stream.of(
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
            ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, BooleanBiFunction.OR)).get();
    */
}
