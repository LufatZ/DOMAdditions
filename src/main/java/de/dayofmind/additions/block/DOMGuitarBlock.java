package de.dayofmind.additions.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.Instrument;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

import static net.minecraft.block.HorizontalFacingBlock.FACING;

public class DOMGuitarBlock extends DOMInstrumentBlock{

    public DOMGuitarBlock(Settings settings) {
        super(settings);
    }

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

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(INSTRUMENT, Instrument.GUITAR).with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return this.getDefaultState().with(INSTRUMENT, Instrument.GUITAR).with(Properties.HORIZONTAL_FACING,state.get(Properties.HORIZONTAL_FACING));
    }
}
