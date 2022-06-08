package de.dayofmind.additions.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;


public class DOMInstrumentBlock extends Block implements Waterloggable {

    public static final BooleanProperty WATERLOGGED;
    public static final EnumProperty<Instrument> INSTRUMENT;
    public static final BooleanProperty POWERED;
    public static final IntProperty NOTE;

    public DOMInstrumentBlock(Settings settings) {
        super(settings);}

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getPlayerFacing().getOpposite());
    }

    private void playNote(@Nullable Entity entity, World world, BlockPos pos) {
        if (world.getBlockState(pos.up()).isAir()) {
            world.addSyncedBlockEvent(pos, this, 0, 0);
            world.emitGameEvent(entity, GameEvent.NOTE_BLOCK_PLAY, pos);
        }
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean bl = world.isReceivingRedstonePower(pos);
        if (bl != state.get(POWERED)) {
            if (bl) {
                this.playNote(null, world, pos);
            }
            world.setBlockState(pos, state.with(POWERED, bl), 3);
        }

    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            state = state.cycle(NOTE);
            world.setBlockState(pos, state, 3);
            this.playNote(player, world, pos);
            player.incrementStat(Stats.TUNE_NOTEBLOCK);
            return ActionResult.CONSUME;
        }
    }

    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        int i = state.get(NOTE);
        float f = (float)Math.pow(2.0, (double)(i - 12) / 12.0);
        world.playSound(null, pos, state.get(INSTRUMENT).getSound(), SoundCategory.RECORDS, 3.0F, f);
        world.addParticle(ParticleTypes.NOTE, (double)pos.getX() + 0.5, (double)pos.getY() + 1.2, (double)pos.getZ() + 0.5, (double)i / 24.0, 0.0, 0.0);
        return true;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING,INSTRUMENT, POWERED, NOTE, WATERLOGGED);
    }

    static {
        INSTRUMENT = Properties.INSTRUMENT;
        POWERED = Properties.POWERED;
        NOTE = Properties.NOTE;
        WATERLOGGED = Properties.WATERLOGGED;
    }
}
