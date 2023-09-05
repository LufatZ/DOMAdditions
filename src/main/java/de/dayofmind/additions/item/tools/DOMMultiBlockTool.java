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
    private final TagKey<Block> effectiveBlocks;
    public DOMMultiBlockTool(float attackDamage, float attackSpeed, ToolMaterial material, TagKey<Block> effectiveBlocks, Settings settings) {
        super(attackDamage, attackSpeed, material, effectiveBlocks, settings);
        this.effectiveBlocks = effectiveBlocks;
    }

    @Override
    public boolean postMine(ItemStack stack, World world, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!world.isClient) {
            int radius = 1;
            Direction playerFacing = Direction.fromRotation(miner.getYaw());
            float verticalRotation = miner.getPitch();

                for (int y = -radius; y <= radius; y++) {
                    for (int x = -radius; x <= radius; x++)  {
                        BlockPos targetPos;
                        if (verticalRotation >= 45.0f || verticalRotation <= -45.0f ) {
                            targetPos = pos.add(x, 0, y);
                        }
                        else if (playerFacing == Direction.SOUTH || playerFacing == Direction.NORTH){
                            targetPos = pos.add(y, x, 0);
                        }
                        else {
                            targetPos = pos.add(0, y, -x);
                        }
                        BlockState targetState = world.getBlockState(targetPos);

                        if (!world.isAir(targetPos) && targetState.getHardness(world, targetPos) >= 0 && targetState.isIn(this.effectiveBlocks)) {
                            world.breakBlock(targetPos, true, miner);
                        }
                    }
                }

        }
        return super.postMine(stack, world, state, pos, miner);
    }
}