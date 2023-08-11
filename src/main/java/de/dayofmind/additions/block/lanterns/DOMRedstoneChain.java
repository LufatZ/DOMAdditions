package de.dayofmind.additions.block.lanterns;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChainBlock;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.joml.Vector3f;

public class DOMRedstoneChain extends ChainBlock {
    public static final IntProperty POWER = IntProperty.of("power", 0, 15);

    public DOMRedstoneChain(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(POWER, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(POWER);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        int maxPower = 0;

        BlockPos neighborPos = pos.offset(direction);
        BlockState neighborState = world.getBlockState(neighborPos);
        int power = neighborState.getWeakRedstonePower(world, neighborPos, direction);
        maxPower = Math.max(maxPower, power);

        for (Direction facing : Direction.values()) {
            if (facing == direction.getOpposite()) {
                continue; // Überspringe die Richtung, aus der die Abfrage kommt
            }

            BlockPos nextNeighborPos = neighborPos.offset(facing);
            BlockState nextNeighborState = world.getBlockState(nextNeighborPos);
            int nextPower = nextNeighborState.getWeakRedstonePower(world, nextNeighborPos, facing);
            maxPower = Math.max(maxPower, nextPower);
        }

        return maxPower;
    }


    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        int power = state.get(POWER);
        if (power > 0) {
            float red = 1.0f; // Roter Farbanteil
            float green = 0.0f; // Grüner Farbanteil
            float blue = 0.0f; // Blauer Farbanteil
            float scale = 1.0f; // Partikelgröße

            DustParticleEffect particle = new DustParticleEffect(new Vector3f(red, green, blue), scale);
            world.addParticle(particle, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        int maxPower = 0;

        for (Direction facing : Direction.values()) {
            BlockPos neighborPos = pos.offset(facing);
            int power = world.getEmittedRedstonePower(neighborPos, facing);
            maxPower = Math.max(maxPower, power);
        }
//TODO fix this shit
        int currentPower = state.get(POWER);
        if (maxPower != currentPower) {
            world.setBlockState(pos, state.with(POWER, maxPower), 3);
            //world.updateNeighborsExcept(pos, this, Direction.fromVector(fromPos.subtract(pos)));
        }
    }

}
