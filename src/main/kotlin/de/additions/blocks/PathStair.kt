package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.block.StairsBlock
import net.minecraft.block.enums.BlockHalf
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import java.util.stream.IntStream

/**
 * Represents a stair version of a path block.
 * Path stairs are slightly shorter than regular stairs to match the height of path blocks.
 * This class provides custom outline shapes to achieve the desired height.
 *
 * @param blockstate The base block state of the stair.
 * @param settings The settings for the block.
 */
open class PathStair(blockstate: BlockState, settings: Settings) : StairsBlock(blockstate, settings) {

    companion object {
        /** The base shape for the top part of the stair, one pixel shorter than a regular stair. */
        protected val TOP_SHAPE: VoxelShape = PathSlab.TOP_SHAPE
        /** The base shape for the bottom part of the stair, one pixel shorter than a regular stair. */
        protected val BOTTOM_SHAPE: VoxelShape = PathSlab.BOTTOM_SHAPE

        /** Corner shapes for the bottom part of the stair. */
        protected val BOTTOM_NORTH_WEST_CORNER_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 8.0, 8.0, 8.0)
        protected val BOTTOM_SOUTH_WEST_CORNER_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 8.0, 8.0, 8.0, 16.0)
        protected val BOTTOM_NORTH_EAST_CORNER_SHAPE: VoxelShape = createCuboidShape(8.0, 0.0, 0.0, 16.0, 8.0, 8.0)
        protected val BOTTOM_SOUTH_EAST_CORNER_SHAPE: VoxelShape = createCuboidShape(8.0, 0.0, 8.0, 16.0, 8.0, 16.0)

        /** Corner shapes for the top part of the stair, adjusted to be one pixel shorter. */
        protected val TOP_NORTH_WEST_CORNER_SHAPE: VoxelShape = createCuboidShape(0.0, 7.0, 0.0, 8.0, 15.0, 8.0)
        protected val TOP_SOUTH_WEST_CORNER_SHAPE: VoxelShape = createCuboidShape(0.0, 7.0, 8.0, 8.0, 15.0, 16.0)
        protected val TOP_NORTH_EAST_CORNER_SHAPE: VoxelShape = createCuboidShape(8.0, 7.0, 0.0, 16.0, 15.0, 8.0)
        protected val TOP_SOUTH_EAST_CORNER_SHAPE: VoxelShape = createCuboidShape(8.0, 7.0, 8.0, 16.0, 15.0, 16.0)

        /** Indices used to look up the correct shape combination based on the block's state. */
        private val SHAPE_INDICES = intArrayOf(12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8)

        /** An array of pre-composed shapes for the top half of the stair. */
        protected val TOP_SHAPES: Array<VoxelShape> = composeShapes(
            TOP_SHAPE,
            BOTTOM_NORTH_WEST_CORNER_SHAPE,
            BOTTOM_NORTH_EAST_CORNER_SHAPE,
            BOTTOM_SOUTH_WEST_CORNER_SHAPE,
            BOTTOM_SOUTH_EAST_CORNER_SHAPE
        )

        /** An array of pre-composed shapes for the bottom half of the stair. */
        protected val BOTTOM_SHAPES: Array<VoxelShape> = composeShapes(
            BOTTOM_SHAPE,
            TOP_NORTH_WEST_CORNER_SHAPE,
            TOP_NORTH_EAST_CORNER_SHAPE,
            TOP_SOUTH_WEST_CORNER_SHAPE,
            TOP_SOUTH_EAST_CORNER_SHAPE
        )

        /**
         * Composes an array of 16 voxel shapes by combining a base shape with corner shapes.
         */
        private fun composeShapes(
            base: VoxelShape,
            northWest: VoxelShape,
            northEast: VoxelShape,
            southWest: VoxelShape,
            southEast: VoxelShape
        ): Array<VoxelShape> {
            return IntStream.range(0, 16)
                .mapToObj { i -> composeShape(i, base, northWest, northEast, southWest, southEast) }
                .toArray { size -> arrayOfNulls<VoxelShape>(size) }
        }

        /**
         * Composes a single voxel shape by combining a base shape with corner shapes based on a bitmask.
         */
        private fun composeShape(
            i: Int,
            base: VoxelShape,
            northWest: VoxelShape,
            northEast: VoxelShape,
            southWest: VoxelShape,
            southEast: VoxelShape
        ): VoxelShape {
            var voxelShape = base

            if (i and 1 != 0) {
                voxelShape = VoxelShapes.union(voxelShape, northWest)
            }
            if (i and 2 != 0) {
                voxelShape = VoxelShapes.union(voxelShape, northEast)
            }
            if (i and 4 != 0) {
                voxelShape = VoxelShapes.union(voxelShape, southWest)
            }
            if (i and 8 != 0) {
                voxelShape = VoxelShapes.union(voxelShape, southEast)
            }

            return voxelShape
        }
    }

    /**
     * Calculates an index into the [SHAPE_INDICES] array based on the block's shape and facing direction.
     */
    private fun getShapeIndexIndex(state: BlockState): Int {
        return state.get(SHAPE).ordinal * 4 + state.get(FACING).horizontalQuarterTurns
    }

    /**
     * Gets the outline shape of the stair based on its state.
     * Overridden to provide custom shapes that are one pixel shorter than regular stairs.
     */
    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return (if (state.get(HALF) == BlockHalf.TOP) TOP_SHAPES else BOTTOM_SHAPES)[SHAPE_INDICES[getShapeIndexIndex(state)]]
    }
}