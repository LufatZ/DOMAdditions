package de.additions.mixin;

import de.additions.blocks.BlockRegistry;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashSet;
import java.util.Set;


@Mixin(BubbleColumnBlock.class)
public abstract class BubbleColumnBlockMixin {

    @Unique
    private static Set<Block> MAGMA_VARIANTS = null;

    @Unique
    private static Set<Block> getMagmaVariants() {
        if (MAGMA_VARIANTS == null) {
            MAGMA_VARIANTS = new HashSet<>(BlockRegistry.getRegisteredMagmaBlocks());
            MAGMA_VARIANTS.add(Blocks.MAGMA_BLOCK);
        }
        return MAGMA_VARIANTS;
    }

    @Redirect(
            method = "getColumnState",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z",
                    ordinal = 2  // 0 = BUBBLE_COLUMN, 1 = SOUL_SAND, 2 = MAGMA_BLOCK
            )
    )
    private static boolean redirectMagmaCheck(BlockState state, Block block) {
        return getMagmaVariants().contains(state.getBlock());
    }
}