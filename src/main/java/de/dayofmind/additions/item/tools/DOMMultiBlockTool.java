package de.dayofmind.additions.item.tools;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class DOMMultiBlockTool extends MiningToolItem {


    public DOMMultiBlockTool(float attackDamage, float attackSpeed, ToolMaterial material, TagKey<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient) {
            int radius = 1;
            Direction playerFacing = Direction.fromRotation(miner.getYaw());
            float verticalRotation = miner.getPitch();

            if ((playerFacing == Direction.NORTH || playerFacing == Direction.SOUTH) && !(verticalRotation >= 45.0f || verticalRotation <= -45.0f )) {
                for (int y = -radius; y <= radius; y++) {
                    for (int x = -radius; x <= radius; x++) {
                        for (int z = -0; z <= 0; z++) {
                            BlockPos targetPos = pos.add(x, y, z);
                            BlockState targetState = world.getBlockState(targetPos);

                            if (!world.isAir(targetPos) && targetState.getHardness(world, targetPos) >= 0) {
                                world.breakBlock(targetPos, true, miner);
                            }
                        }
                    }
                }
            } else if ((playerFacing == Direction.EAST || playerFacing == Direction.WEST) && !(verticalRotation >= 45.0f || verticalRotation <= -45.0f )) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -0; z <= 0; z++) {
                        for (int x = -radius; x <= radius; x++) {
                            BlockPos targetPos = pos.add(z, y, -x);
                            BlockState targetState = world.getBlockState(targetPos);

                            if (!world.isAir(targetPos) && targetState.getHardness(world, targetPos) >= 0) {
                                world.breakBlock(targetPos, true, miner);
                            }
                        }
                    }
                }
            } if (verticalRotation >= 45.0f || verticalRotation <= -45.0f ){ // playerFacing == Direction.DOWN or .UP
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -0; z <= 0; z++) {
                        for (int y = -radius; y <= radius; y++) {
                            BlockPos targetPos = pos.add(x, z, y);
                            BlockState targetState = world.getBlockState(targetPos);

                            if (!world.isAir(targetPos) && targetState.getHardness(world, targetPos) >= 0) {
                                world.breakBlock(targetPos, true, miner);
                            }
                        }
                    }
                }
            }
        }
        return super.postMine(stack, world, state, pos, miner);
    }
}