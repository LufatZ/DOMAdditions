package de.dayofmind.additions.mixins;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import de.dayofmind.additions.block.DOMBlocks;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static net.minecraft.block.StairsBlock.SHAPE;
import static net.minecraft.block.StairsBlock.WATERLOGGED;

@Mixin(ShovelItem.class)
public class DOMShovelMixin {
    private static final Map<Block, BlockState> PATH_STATES2 = Maps.newHashMap(new ImmutableMap.Builder<Block, BlockState>().put(DOMBlocks.DIRT_SLAB, DOMBlocks.DIRT_PATH_SLAB.getDefaultState()).put(DOMBlocks.GRASS_SLAB, DOMBlocks.DIRT_PATH_SLAB.getDefaultState()).put(DOMBlocks.DIRT_STAIR, DOMBlocks.DIRT_PATH_STAIR.getDefaultState()).put(DOMBlocks.GRASS_STAIR, DOMBlocks.DIRT_PATH_STAIR.getDefaultState()).build());

    @Inject(at = @At("HEAD"), method = "useOnBlock")
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (context.getSide() != Direction.DOWN && world.getBlockState(blockPos).isIn(BlockTags.DIRT)&& (blockState.getBlock() instanceof StairsBlock || blockState.getBlock() instanceof SlabBlock)) {
            PlayerEntity playerEntity = context.getPlayer();
            BlockState blockState4 = null;
            if(blockState.getBlock() instanceof StairsBlock){
                blockState4 = PATH_STATES2.get(blockState.getBlock())
                        .with(StairsBlock.FACING, blockState.get(StairsBlock.FACING))
                        .with(StairsBlock.HALF, blockState.get(StairsBlock.HALF))
                        .with(StairsBlock.SHAPE, blockState.get(SHAPE))
                        .with(WATERLOGGED, blockState.get(WATERLOGGED));
            }
            if(blockState.getBlock() instanceof SlabBlock){
                blockState4 = PATH_STATES2.get(blockState.getBlock())
                        .with(SlabBlock.TYPE, blockState.get(SlabBlock.TYPE))
                        .with(WATERLOGGED, blockState.get(WATERLOGGED));
            }
            BlockState blockState5 = null;

            if (blockState4 != null && world.getBlockState(blockPos.up()).isAir()) {
                world.playSound(playerEntity, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);
                blockState5 = blockState4;
            } else if (blockState.getBlock() instanceof CampfireBlock && (Boolean)blockState.get(CampfireBlock.LIT)) {
                if (!world.isClient()) {
                    world.syncWorldEvent((PlayerEntity)null, 1009, blockPos, 0);
                }

                CampfireBlock.extinguish(context.getPlayer(), world, blockPos, blockState);
                blockState5 = (BlockState)blockState.with(CampfireBlock.LIT, false);
            }

            if (blockState5 != null) {
                if (!world.isClient) {
                    world.setBlockState(blockPos, blockState5, 11);
                    if (playerEntity != null) {
                        context.getStack().damage(1, playerEntity, (p) -> {
                            p.sendToolBreakStatus(context.getHand());
                        });
                    }
                }
            }
        }
    }
}
