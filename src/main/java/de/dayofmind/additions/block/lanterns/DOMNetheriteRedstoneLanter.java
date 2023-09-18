package de.dayofmind.additions.block.lanterns;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

import static de.dayofmind.additions.block.lanterns.DOMNetheriteLantern.NETHERITE_HANGING_SHAPE;
import static de.dayofmind.additions.block.lanterns.DOMNetheriteLantern.NETHERITE_STANDING_SHAPE;

public class DOMNetheriteRedstoneLanter extends DOMRedstoneLantern{
    public DOMNetheriteRedstoneLanter(Settings settings) {
        super(settings);
    }
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        boolean hanging = state.get(HANGING);
        return hanging ? NETHERITE_HANGING_SHAPE : NETHERITE_STANDING_SHAPE;
    }
}
