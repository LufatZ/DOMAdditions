package de.dayofmind.additions.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.Waterloggable;

public class DOMStairs extends StairsBlock implements Waterloggable {
    public DOMStairs(BlockState baseBlockState, Settings settings) {
        super(baseBlockState, settings);
    }
}
