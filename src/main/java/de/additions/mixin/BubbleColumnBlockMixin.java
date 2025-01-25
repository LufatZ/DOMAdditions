package de.additions.mixin;

import de.additions.blocks.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;


@Mixin(BubbleColumnBlock.class)
public abstract class BubbleColumnBlockMixin {
    @Unique
    private static final Set<Block> MAGMA_VARIANTS = new HashSet<>();
    @Unique
    private static boolean initialized = false;

    @Inject(method = "getBubbleState", at = @At("HEAD"))
    private static void initCache(BlockState state, CallbackInfoReturnable<BlockState> cir) {
        if (!initialized) {
            MAGMA_VARIANTS.addAll(BlockRegistry.getRegisteredMagmaBlocks());
            MAGMA_VARIANTS.add(Blocks.MAGMA_BLOCK);
            initialized = true;
        }
    }

    @ModifyVariable(
            method = "getBubbleState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z", ordinal = 0),
            ordinal = 0,
            argsOnly = true)
    private static BlockState modifyBubbleCheck(BlockState state) {
        return MAGMA_VARIANTS.contains(state.getBlock()) ?
                Blocks.MAGMA_BLOCK.getDefaultState() :
                state;
    }
}