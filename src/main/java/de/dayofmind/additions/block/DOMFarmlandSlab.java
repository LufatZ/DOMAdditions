package de.dayofmind.additions.block;

import net.minecraft.block.*;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.Random;
//TODO springen auf farmland => Farmland zu erde => Steht in Block
public class DOMFarmlandSlab extends DOMShortSlab {

    public static final EnumProperty<SlabType> TYPE;
    public static final BooleanProperty WATERLOGGED;
    public static final IntProperty MOISTURE; //add moisture (de: Feuchtigkeit)
    public static final int MAX_MOISTURE = 7;


    static {//load Block properties
        TYPE = Properties.SLAB_TYPE;
        WATERLOGGED = Properties.WATERLOGGED;
        MOISTURE = Properties.MOISTURE;}

    public DOMFarmlandSlab(Settings settings) {
        super(settings);setDefaultState(getStateManager().getDefaultState().with(WATERLOGGED, false).with(TYPE, SlabType.BOTTOM).with(MOISTURE, 0));
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) { //add Block properties
        stateManager.add(TYPE, WATERLOGGED, MOISTURE);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        int i = state.get(MOISTURE);
        if (DOMFarmlandSlab.isWaterNearby(world, pos) || world.hasRain(pos.up())) {
            if (i < 7) {
                world.setBlockState(pos, (BlockState)state.with(MOISTURE, 7), Block.NOTIFY_LISTENERS);
            }
        } else if (i > 0) {
            world.setBlockState(pos, (BlockState)state.with(MOISTURE, i - 1), Block.NOTIFY_LISTENERS);
        } else if (!DOMFarmlandSlab.hasCrop(world, pos)) {
            DOMFarmlandSlab.setToDirt(state, world, pos);
        }
    }

    private static boolean hasCrop(BlockView world, BlockPos pos) {
        Block block = world.getBlockState(pos.up()).getBlock();
        return block instanceof CropBlock || block instanceof StemBlock || block instanceof AttachedStemBlock;
    }

    private static boolean isWaterNearby(WorldView world, BlockPos pos) {
        for (BlockPos blockPos : BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 1, 4))) {
            if (!world.getFluidState(blockPos).isIn(FluidTags.WATER)) continue;
            return true;
        }
        return false;
    }
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!state.canPlaceAt(world, pos)) {
            DOMFarmlandSlab.setToDirt(state, world, pos);
        }
    }
    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!world.isClient && world.random.nextFloat() < fallDistance - 0.5f && entity instanceof LivingEntity && (entity instanceof PlayerEntity || world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING)) && entity.getWidth() * entity.getWidth() * entity.getHeight() > 0.512f) {
            DOMFarmlandSlab.setToDirt(state, world, pos);
        }
        super.onLandedUpon(world, state, pos, entity, fallDistance);
    }
    public static void setToDirt(BlockState state, World world, BlockPos pos) {
        if(state.getBlock() instanceof SlabBlock)	world.setBlockState(pos, DOMBlocks.DIRT_SLAB.getDefaultState().with(SlabBlock.TYPE, state.get(SlabBlock.TYPE)).with(SlabBlock.WATERLOGGED, state.get(SlabBlock.WATERLOGGED)));
    }

    @Override
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return false;
    }
}
