package de.dayofmind.additions.block.instruments;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DOMFluteBlock extends DOMInstrumentBlock {
    private static final VoxelShape FLUTE_SHAPE;

    public DOMFluteBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(INSTRUMENT, Instrument.FLUTE));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        return FLUTE_SHAPE;
    }

    static {
        FLUTE_SHAPE = VoxelShapes.cuboid(0.125, 0, 0.125, 0.875, 0.625, 0.875);
    }
}
