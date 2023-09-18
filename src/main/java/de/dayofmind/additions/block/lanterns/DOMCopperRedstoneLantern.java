package de.dayofmind.additions.block.lanterns;

import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

import static de.dayofmind.additions.block.lanterns.DOMCopperLantern.COPPER_HANGING_SHAPE;
import static de.dayofmind.additions.block.lanterns.DOMCopperLantern.COPPER_STANDING_SHAPE;

public class DOMCopperRedstoneLantern extends DOMRedstoneLantern{
    public DOMCopperRedstoneLantern(Settings settings) {
        super(settings);
    }
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        boolean hanging = state.get(HANGING);
        return hanging ? COPPER_HANGING_SHAPE : COPPER_STANDING_SHAPE;
    }
}
