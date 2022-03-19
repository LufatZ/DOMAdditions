package de.lufatz.blocks;


import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.Waterloggable;

public class NewStairBlock extends StairsBlock implements Waterloggable {
    public NewStairBlock(BlockState baseBlockState, Settings settings) {
        super(baseBlockState, settings);
    }
}
