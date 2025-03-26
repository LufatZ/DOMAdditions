package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class BigRedstoneLantern(
    settings: Settings?,
) : RedstoneLantern(settings) {
    companion object {
        val BIG_HANGING_SHAPE: VoxelShape =
            VoxelShapes.union(
                createCuboidShape(5.0, 11.0, 5.0, 11.0, 12.0, 11.0),
                createCuboidShape(4.0, 10.0, 4.0, 12.0, 11.0, 12.0),
                createCuboidShape(3.0, 8.0, 3.0, 13.0, 10.0, 13.0),
                createCuboidShape(5.0, 2.0, 5.0, 11.0, 8.0, 11.0),
                createCuboidShape(3.0, 2.0, 12.0, 4.0, 8.0, 13.0),
                createCuboidShape(3.0, 2.0, 3.0, 4.0, 8.0, 4.0),
                createCuboidShape(12.0, 2.0, 3.0, 13.0, 8.0, 4.0),
                createCuboidShape(12.0, 2.0, 12.0, 13.0, 8.0, 13.0),
                createCuboidShape(3.0, 1.0, 3.0, 13.0, 2.0, 13.0),
                createCuboidShape(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            )
        val BIG_STANDING_SHAPE: VoxelShape =
            VoxelShapes.union(
                createCuboidShape(5.0, 11.0, 5.0, 11.0, 12.0, 11.0),
                createCuboidShape(4.0, 10.0, 4.0, 12.0, 11.0, 12.0),
                createCuboidShape(3.0, 8.0, 3.0, 13.0, 10.0, 13.0),
                createCuboidShape(5.0, 2.0, 5.0, 11.0, 8.0, 11.0),
                createCuboidShape(3.0, 2.0, 12.0, 4.0, 8.0, 13.0),
                createCuboidShape(3.0, 2.0, 3.0, 4.0, 8.0, 4.0),
                createCuboidShape(12.0, 2.0, 3.0, 13.0, 8.0, 4.0),
                createCuboidShape(12.0, 2.0, 12.0, 13.0, 8.0, 13.0),
                createCuboidShape(3.0, 1.0, 3.0, 13.0, 2.0, 13.0),
                createCuboidShape(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            )
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?,
    ): VoxelShape = if (state.get(HANGING)) BIG_HANGING_SHAPE else BIG_STANDING_SHAPE
}
