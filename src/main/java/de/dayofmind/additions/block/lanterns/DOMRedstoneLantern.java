package de.dayofmind.additions.block.lanterns;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LanternBlock;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;


public class DOMRedstoneLantern extends LanternBlock {
    public static final BooleanProperty LIT;
    public static final BooleanProperty HANGING;

    public DOMRedstoneLantern(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(HANGING, false).with(WATERLOGGED, false).with(LIT, false));
    }
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        Direction[] var3 = ctx.getPlacementDirections();

        for (Direction direction : var3) {
            if (direction.getAxis() == Direction.Axis.Y) {
                BlockState blockState = this.getDefaultState().with(HANGING, direction == Direction.UP);
                if (blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                    return blockState.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER).with(LIT, isPowered(ctx));
                }
            }
        }

        return null;
    }
    private static boolean isPowered(ItemPlacementContext ctx) {
        return ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos());
    }
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        if (!world.isClient) {
            boolean bl = state.get(LIT);
            if (bl != world.isReceivingRedstonePower(pos)) {
                if (bl) {
                    world.scheduleBlockTick(pos, this, 4);
                } else {
                    world.setBlockState(pos, state.cycle(LIT), 2);
                }
            }

        }
    }
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(LIT) && !world.isReceivingRedstonePower(pos)) {
            world.setBlockState(pos, state.cycle(LIT), 2);
        }

    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HANGING, LIT, WATERLOGGED);
    }

    static {
        LIT = RedstoneTorchBlock.LIT;
        HANGING = Properties.HANGING;
    }
}
