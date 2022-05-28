package de.dayofmind.additions.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class DOMInstrumentBlock extends Block implements Waterloggable {
    public DOMInstrumentBlock(Settings settings) {
        super(settings);
    }
    protected static final VoxelShape GUITAR_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(7, 1, 6, 10, 2, 7),
            Block.createCuboidShape(11, 1, 7, 12, 9, 10),
            Block.createCuboidShape(5, 1, 7, 6, 9, 10),
            Block.createCuboidShape(10, 0, 7, 11, 10, 10),
            Block.createCuboidShape(6, 0, 7, 7, 10, 10),
            Block.createCuboidShape(7, 0, 8, 10, 11, 10),
            Block.createCuboidShape(4, 2, 7, 5, 5, 10),
            Block.createCuboidShape(12, 2, 7, 13, 5, 10),
            Block.createCuboidShape(4, 6, 7, 5, 8, 10),
            Block.createCuboidShape(12, 6, 7, 13, 8, 10),
            Block.createCuboidShape(9, 0, 7, 10, 4, 8),
            Block.createCuboidShape(7, 0, 7, 8, 4, 8),
            Block.createCuboidShape(7, 6, 7, 8, 11, 8),
            Block.createCuboidShape(9, 6, 7, 10, 11, 8),
            Block.createCuboidShape(8, 0, 7, 9, 3, 8),
            Block.createCuboidShape(8, 7, 7, 9, 11, 8),
            Block.createCuboidShape(7, 19, 7, 10, 20, 8),
            Block.createCuboidShape(9, 17, 7, 10, 18, 8),
            Block.createCuboidShape(7, 17, 7, 8, 18, 8),
            Block.createCuboidShape(8, 16, 7, 9, 19, 8),
            Block.createCuboidShape(7, 11, 7, 10, 13, 9),
            Block.createCuboidShape(7, 13, 7, 10, 16, 8),
            Block.createCuboidShape(6, 16, 7, 7, 19, 8),
            Block.createCuboidShape(10, 16, 7, 11, 19, 8),
            Block.createCuboidShape(7.5, 2, 6.5, 7.5, 18, 7),
            Block.createCuboidShape(9.5, 2, 6.5, 9.5, 18, 7),
            Block.createCuboidShape(8.5, 2, 6.5, 8.5, 19, 7)

    );

    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if(state.isOf(DOMBlocks.GUITAR)) {
            return GUITAR_SHAPE;
        }
        return null;
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_GUITAR, 1,1);
        return ActionResult.SUCCESS;
    }
}
