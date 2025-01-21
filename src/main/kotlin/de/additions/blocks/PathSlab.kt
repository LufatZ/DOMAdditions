package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.SlabBlock
import net.minecraft.block.enums.SlabType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

class PathSlab(settings: Settings) : SlabBlock(settings) {

    companion object {
        val TOP_SHAPE: VoxelShape = createCuboidShape(0.0, 8.0, 0.0, 16.0, 15.0, 16.0)
        val BOTTOM_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0)
        val FULL_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 15.0, 16.0)
    }

    override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return when (state.get(TYPE)) {
            SlabType.TOP -> TOP_SHAPE
            SlabType.BOTTOM -> BOTTOM_SHAPE
            else -> FULL_SHAPE
        }
    }
}