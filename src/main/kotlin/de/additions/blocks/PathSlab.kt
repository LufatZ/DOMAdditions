package de.additions.blocks

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter

/**
 * Represents a slab version of a path block.
 * Path slabs are slightly shorter than regular slabs to match the height of path blocks.
 *
 * @param settings The settings for the block.
 */
class PathSlab(settings: Properties) : SlabBlock(settings) {

    companion object {
        /** The outline shape for the top slab, one pixel shorter than a regular slab. */
        val TOP_SHAPE: VoxelShape = box(0.0, 8.0, 0.0, 16.0, 15.0, 16.0)
        /** The outline shape for the bottom slab, one pixel shorter than a regular slab. */
        val BOTTOM_SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 7.0, 16.0)
        /** The outline shape for a double slab, one pixel shorter than a full block. */
        val FULL_SHAPE: VoxelShape = box(0.0, 0.0, 0.0, 16.0, 15.0, 16.0)
    }

    /**
     * Gets the outline shape of the slab based on its state.
     * Overridden to provide custom shapes that are one pixel shorter than regular slabs.
     */
    override fun getShape(state: BlockState, view: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape {
        return when (state.getValue(TYPE)) {
            SlabType.TOP -> TOP_SHAPE
            SlabType.BOTTOM -> BOTTOM_SHAPE
            else -> FULL_SHAPE
        }
    }
}