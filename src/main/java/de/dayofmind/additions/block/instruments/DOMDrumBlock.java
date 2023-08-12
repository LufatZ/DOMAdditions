package de.dayofmind.additions.block.instruments;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class DOMDrumBlock extends DOMInstrumentBlock {
    private static final VoxelShape DRUM_SHAPE;

    public DOMDrumBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(INSTRUMENT, Instrument.BASEDRUM));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext ctx) {
        return DRUM_SHAPE;
    }

    static {
        DRUM_SHAPE = VoxelShapes.cuboid(0.0625, 0, 0.0625, 0.9375, 0.375, 0.9375);
    }
}
