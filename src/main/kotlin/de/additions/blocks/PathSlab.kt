package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.SlabBlock
import net.minecraft.block.enums.SlabType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.world.BlockView

/**
 * Represents a slab version of a path block.
 * Path slabs are slightly shorter than regular slabs to match the height of path blocks.
 *
 * @param settings The settings for the block.
 */
class PathSlab(settings: Settings) : SlabBlock(settings) {

    companion object {
        /** The outline shape for the top slab, one pixel shorter than a regular slab. */
        val TOP_SHAPE: VoxelShape = createCuboidShape(0.0, 8.0, 0.0, 16.0, 15.0, 16.0)
        /** The outline shape for the bottom slab, one pixel shorter than a regular slab. */
        val BOTTOM_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 7.0, 16.0)
        /** The outline shape for a double slab, one pixel shorter than a full block. */
        val FULL_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 16.0, 15.0, 16.0)
    }

    /**
     * Gets the outline shape of the slab based on its state.
     * Overridden to provide custom shapes that are one pixel shorter than regular slabs.
     */
    override fun getOutlineShape(state: BlockState, view: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return when (state.get(TYPE)) {
            SlabType.TOP -> TOP_SHAPE
            SlabType.BOTTOM -> BOTTOM_SHAPE
            else -> FULL_SHAPE
        }
    }
}