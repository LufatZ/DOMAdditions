package de.dayofmind.additions.block.slabs;

import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public class DOMShortSlab extends SlabBlock implements Waterloggable {
    public DOMShortSlab(Settings settings) {
        super(settings);
    }
    public static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0);
    public static final VoxelShape TOP_SHAPE =    Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 15.0, 16.0);
    public static final VoxelShape DOUBLE_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 15.0, 16.0);

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        SlabType slabType = state.get(TYPE);
        switch (slabType) {
            case DOUBLE -> {return DOUBLE_SHAPE;}
            case TOP -> {return TOP_SHAPE;}
            default -> {return BOTTOM_SHAPE;}
        }
    }
}
