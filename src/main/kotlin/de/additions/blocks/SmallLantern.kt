package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.LanternBlock
import net.minecraft.block.ShapeContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

class SmallLantern(
    settings: Settings?,
) : LanternBlock(settings) {
    companion object {
        val SMALL_HANGING_SHAPE: VoxelShape =
            VoxelShapes.union(
                createCuboidShape(7.0, 3.0, 7.0, 9.0, 6.0, 9.0),
                createCuboidShape(7.0, 2.0, 7.0, 9.0, 3.0, 9.0),
                createCuboidShape(7.0, 9.0, 7.0, 9.0, 10.0, 9.0),
                createCuboidShape(6.0, 0.0, 6.0, 10.0, 2.0, 10.0),
                createCuboidShape(7.0, 6.0, 7.0, 9.0, 8.0, 9.0),
                createCuboidShape(8.0, 2.0, 6.0, 9.0, 3.0, 7.0),
                createCuboidShape(7.0, 2.0, 9.0, 8.0, 3.0, 10.0),
                createCuboidShape(7.0, 6.0, 9.0, 8.0, 7.0, 10.0),
                createCuboidShape(8.0, 6.0, 6.0, 9.0, 7.0, 7.0),
                createCuboidShape(8.0, 3.0, 5.0, 9.0, 6.0, 6.0),
                createCuboidShape(7.0, 3.0, 10.0, 8.0, 6.0, 11.0),
                createCuboidShape(7.5, 8.0, 7.5, 8.5, 9.0, 8.5),
            )
        val SMALL_STANDING_SHAPE: VoxelShape =
            VoxelShapes.union(
                createCuboidShape(7.0, 3.0, 7.0, 9.0, 6.0, 9.0),
                createCuboidShape(7.0, 2.0, 7.0, 9.0, 3.0, 9.0),
                createCuboidShape(7.0, 9.0, 7.0, 9.0, 10.0, 9.0),
                createCuboidShape(6.0, 0.0, 6.0, 10.0, 2.0, 10.0),
                createCuboidShape(7.0, 6.0, 7.0, 9.0, 8.0, 9.0),
                createCuboidShape(8.0, 2.0, 6.0, 9.0, 3.0, 7.0),
                createCuboidShape(7.0, 2.0, 9.0, 8.0, 3.0, 10.0),
                createCuboidShape(7.0, 6.0, 9.0, 8.0, 7.0, 10.0),
                createCuboidShape(8.0, 6.0, 6.0, 9.0, 7.0, 7.0),
                createCuboidShape(8.0, 3.0, 5.0, 9.0, 6.0, 6.0),
                createCuboidShape(7.0, 3.0, 10.0, 8.0, 6.0, 11.0),
                createCuboidShape(7.5, 8.0, 7.5, 8.5, 9.0, 8.5),
            )
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?,
    ): VoxelShape = if (state.get(HANGING)) SMALL_HANGING_SHAPE else SMALL_STANDING_SHAPE
}
