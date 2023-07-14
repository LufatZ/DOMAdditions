package de.dayofmind.additions.mixins;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static de.dayofmind.additions.block.DOMBlocksRegistry.*;
import static net.minecraft.state.property.Properties.WATERLOGGED;

@Mixin(ShovelItem.class)
public class DOMShovelMixin {
    @Unique
    private static final Map<Block, Block> BLOCK_SWAP_MAP = ImmutableMap.<Block, Block>builder()
            .put(Blocks.DIRT_PATH, Blocks.DIRT)
            .put(GRASS_SLAB, DIRT_PATH_SLAB)
            .put(GRASS_STAIR, DIRT_PATH_STAIR)
            .put(DIRT_SLAB, DIRT_PATH_SLAB)
            .put(DIRT_PATH_SLAB, DIRT_SLAB)
            .put(DIRT_STAIR, DIRT_PATH_STAIR)
            .put(DIRT_PATH_STAIR, DIRT_STAIR)
            .build();

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);

        Block newState = BLOCK_SWAP_MAP.get(blockState.getBlock());

        if (newState != null) {
            BlockState newBlockState;

            if (newState instanceof StairsBlock) {
                // If the new block is a stairs block, copy the properties from the original block
                StairsBlock stairsBlock = (StairsBlock) newState;
                newBlockState = stairsBlock.getDefaultState()
                        .with(StairsBlock.FACING, blockState.get(StairsBlock.FACING))
                        .with(StairsBlock.HALF, blockState.get(StairsBlock.HALF))
                        .with(StairsBlock.SHAPE, blockState.get(StairsBlock.SHAPE))
                        .with(WATERLOGGED, blockState.get(WATERLOGGED));
            } else if (newState instanceof SlabBlock) {
                // If the new block is a slab block, copy the properties from the original block
                SlabBlock slabBlock = (SlabBlock) newState;
                newBlockState = slabBlock.getDefaultState()
                        .with(SlabBlock.TYPE, blockState.get(SlabBlock.TYPE))
                        .with(WATERLOGGED, blockState.get(WATERLOGGED));
            } else {
                // For other blocks, use the default state
                newBlockState = newState.getDefaultState();
            }

            // Replace the current block with the new block
            world.setBlockState(blockPos, newBlockState, 11);

            // Play sound and damage the player's tool
            playSoundAndDamageItem(context.getPlayer(), world, blockPos);

            // Set the return value to success
            cir.setReturnValue(ActionResult.SUCCESS);
        }
    }

    @Unique
    private void playSoundAndDamageItem(PlayerEntity playerEntity, World world, BlockPos pos) {
        // Play the shovel flatten sound effect
        world.playSound(playerEntity, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

        if (playerEntity != null) {
            // Damage the player's tool
            playerEntity.getMainHandStack().damage(1, playerEntity, (p) -> p.sendToolBreakStatus(playerEntity.getActiveHand()));
        }
    }
}
