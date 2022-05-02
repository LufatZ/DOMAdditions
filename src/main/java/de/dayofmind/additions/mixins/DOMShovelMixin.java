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
import net.minecraft.world.WorldEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

import static net.minecraft.block.StairsBlock.*;

@Mixin(ShovelItem.class)
public class DOMShovelMixin {
    private static final Map<Block, BlockState> PATH_STATES = Maps.newHashMap(new ImmutableMap.Builder<Block, BlockState>().put(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.DIRT, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.PODZOL, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.COARSE_DIRT, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.MYCELIUM, Blocks.DIRT_PATH.getDefaultState()).put(Blocks.ROOTED_DIRT, Blocks.DIRT_PATH.getDefaultState()).build());
    private static final Map<Block, BlockState>  PATH_STATES2 = Maps.newHashMap(new ImmutableMap.Builder<Block, BlockState>().put(DOMBlocks.DIRT_SLAB, DOMBlocks.DIRT_PATH_SLAB.getDefaultState()).put(DOMBlocks.GRASS_SLAB, DOMBlocks.DIRT_PATH_SLAB.getDefaultState()).build());
    private static final Map<Block, BlockState>  PATH_STATES3 = Maps.newHashMap(new ImmutableMap.Builder<Block, BlockState>().put(DOMBlocks.DIRT_STAIR, DOMBlocks.DIRT_PATH_STAIR.getDefaultState()).put(DOMBlocks.GRASS_STAIR, DOMBlocks.DIRT_PATH_STAIR.getDefaultState()).build());

    //TODO scramble and optimize (possibly register or change new tags)

    @Inject(at = @At("HEAD"), method = "useOnBlock", cancellable = true)
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (context.getSide() != Direction.DOWN) {
            PlayerEntity playerEntity = context.getPlayer();
            BlockState blockState2 = PATH_STATES.get(blockState.getBlock());
            BlockState blockState3 = null;
            if (blockState2 != null && world.getBlockState(blockPos.up()).isAir()) {
                world.playSound(playerEntity, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                blockState3 = blockState2;
            } else if (blockState.getBlock() instanceof CampfireBlock && blockState.get(CampfireBlock.LIT).booleanValue()) {
                if (!world.isClient()) {
                    world.syncWorldEvent(null, WorldEvents.FIRE_EXTINGUISHED, blockPos, 0);
                }
                CampfireBlock.extinguish(context.getPlayer(), world, blockPos, blockState);
                blockState3 = blockState.with(CampfireBlock.LIT, false);
            }
            if (blockState3 != null) {
                if (!world.isClient) {
                    world.setBlockState(blockPos, blockState3, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    if (playerEntity != null) {
                        context.getStack().damage(1, playerEntity, p -> p.sendToolBreakStatus(context.getHand()));
                    }
                }
            }
        }
        if (context.getSide() != Direction.DOWN && blockState.getBlock() instanceof StairsBlock && world.getBlockState(blockPos).isIn(BlockTags.DIRT)) {
            BlockState blockState4 = PATH_STATES3.get(blockState.getBlock()).with(FACING, blockState.get(FACING)).with(HALF, blockState.get(StairsBlock.HALF)).with(StairsBlock.WATERLOGGED, blockState.get(StairsBlock.WATERLOGGED)).with(SHAPE, blockState.get(SHAPE));
            BlockState blockState5 = null;
            PlayerEntity playerEntity = context.getPlayer();

            if (blockState4 != null && world.getBlockState(blockPos.up()).isAir()) {
                world.playSound(playerEntity, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                blockState5 = blockState4;
            }
            if (blockState5 != null) {
                if (!world.isClient) {
                    world.setBlockState(blockPos, blockState5, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    if (playerEntity != null) {
                        context.getStack().damage(1, playerEntity, p -> p.sendToolBreakStatus(context.getHand()));
                    }
                }
            }
        }
        if (context.getSide() != Direction.DOWN && blockState.getBlock() instanceof SlabBlock && world.getBlockState(blockPos).isIn(BlockTags.DIRT)) {
            BlockState blockState4 = PATH_STATES2.get(blockState.getBlock()).with(SlabBlock.TYPE, blockState.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, blockState.get(SlabBlock.WATERLOGGED));
            BlockState blockState5 = null;
            PlayerEntity playerEntity = context.getPlayer();

            if (blockState4 != null && world.getBlockState(blockPos.up()).isAir()) {
                world.playSound(playerEntity, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                blockState5 = blockState4;
            }
            if (blockState5 != null) {
                if (!world.isClient) {
                    world.setBlockState(blockPos, blockState5, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
                    if (playerEntity != null) {
                        context.getStack().damage(1, playerEntity, p -> p.sendToolBreakStatus(context.getHand()));
                    }
                }
            }
        }
    }
}
