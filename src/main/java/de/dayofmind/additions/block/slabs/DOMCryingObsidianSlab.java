package de.dayofmind.additions.block.slabs;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class DOMCryingObsidianSlab extends SlabBlock{
    public DOMCryingObsidianSlab(Settings settings) {
        super(settings);
    }
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        SlabType slabType = state.get(TYPE);
        if (random.nextInt(5) == 0) {
            Direction direction = Direction.random(random);
            if (direction != Direction.UP) {
                BlockPos blockPos = pos.offset(direction);
                BlockState blockState = world.getBlockState(blockPos);
                if (!state.isOpaque() || !blockState.isSideSolidFullSquare(world, blockPos, direction.getOpposite())) {
                    double d = direction.getOffsetX() == 0 ? random.nextDouble() : 0.5 + (double) direction.getOffsetX() * 0.6;
                    double e = direction.getOffsetY() == 0 ? random.nextDouble() : 0.5 + (double) direction.getOffsetY() * 0.6;
                    double f = direction.getOffsetZ() == 0 ? random.nextDouble() : 0.5 + (double) direction.getOffsetZ() * 0.6;

                    if (slabType == SlabType.DOUBLE){
                        world.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double) pos.getX() + d, (double) pos.getY() + e, (double) pos.getZ() + f, 0.0, 0.0, 0.0);
                    }
                    if (slabType == SlabType.BOTTOM){
                        world.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double) pos.getX() + d, (double) pos.getY() + 0.3, (double) pos.getZ() + f, 0.0, 0.0, 0.0);
                    }
                    if (slabType == SlabType.TOP){
                        world.addParticle(ParticleTypes.DRIPPING_OBSIDIAN_TEAR, (double) pos.getX() + d, (double) pos.getY() + 0.8, (double) pos.getZ() + f, 0.0, 0.0, 0.0);
                    }
                }
            }
        }
    }
}
