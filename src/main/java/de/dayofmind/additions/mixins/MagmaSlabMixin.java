package de.dayofmind.additions.mixins;

import de.dayofmind.additions.block.DOMBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import static net.minecraft.block.BubbleColumnBlock.DRAG;

@Mixin(BubbleColumnBlock.class)
public class MagmaSlabMixin {

    //TODO temporär überschreiben... später bessere lösung

    /**
     * @author Mojang
     */
    @Overwrite
        public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos.down());
            return blockState.isOf(Blocks.BUBBLE_COLUMN) || blockState.isOf(DOMBlocks.MAGMA_SLAB) || blockState.isOf(Blocks.MAGMA_BLOCK) || blockState.isOf(Blocks.SOUL_SAND);
        }
    /**
     * @author Mojang
     */
    @Overwrite
    private static BlockState getBubbleState(BlockState state) {
        if (state.isOf(Blocks.BUBBLE_COLUMN)) {
            return state;
        } else if (state.isOf(Blocks.SOUL_SAND)) {
            return (BlockState) Blocks.BUBBLE_COLUMN.getDefaultState().with(DRAG, false);
        }else {
            return state.isOf(Blocks.MAGMA_BLOCK) || state.isOf(DOMBlocks.MAGMA_SLAB) ? (BlockState)Blocks.BUBBLE_COLUMN.getDefaultState().with(DRAG, true) : Blocks.WATER.getDefaultState();
        }
    }
}

