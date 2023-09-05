package de.dayofmind.additions.block.lanterns;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class DOMRedstoneLantern extends LanternBlock {
    public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

    public DOMRedstoneLantern(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(HANGING, false).with(WATERLOGGED, false).with(LIT, false));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        Direction[] directions = ctx.getPlacementDirections();

        for (Direction direction : directions) {
            if (direction.getAxis() == Direction.Axis.Y) {
                BlockState blockState = getDefaultState().with(HANGING, direction == Direction.UP);
                if (blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                    boolean powered = ctx.getWorld().getReceivedRedstonePower(ctx.getBlockPos()) > 0;
                    return blockState.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER).with(LIT, powered);
                }
            }
        }

        return null;
    }



    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        if (!world.isClient) {
            boolean powered = world.getReceivedRedstonePower(pos) > 0;
            if (powered != Boolean.TRUE.equals(state.get(LIT))) {
                world.setBlockState(pos, state.with(LIT, powered), Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HANGING, WATERLOGGED, LIT);
    }
}
