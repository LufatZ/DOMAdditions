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

open class PathStair(blockstate: BlockState, settings: Settings) : StairsBlock(blockstate, settings) {

    companion object {
        protected val TOP_SHAPE: VoxelShape = PathSlab.TOP_SHAPE
        protected val BOTTOM_SHAPE: VoxelShape = PathSlab.BOTTOM_SHAPE

        protected val BOTTOM_NORTH_WEST_CORNER_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 0.0, 8.0, 8.0, 8.0)
        protected val BOTTOM_SOUTH_WEST_CORNER_SHAPE: VoxelShape = createCuboidShape(0.0, 0.0, 8.0, 8.0, 8.0, 16.0)
        protected val TOP_NORTH_WEST_CORNER_SHAPE: VoxelShape = createCuboidShape(0.0, 7.0, 0.0, 8.0, 15.0, 8.0)
        protected val TOP_SOUTH_WEST_CORNER_SHAPE: VoxelShape = createCuboidShape(0.0, 7.0, 8.0, 8.0, 15.0, 16.0)
        protected val BOTTOM_NORTH_EAST_CORNER_SHAPE: VoxelShape = createCuboidShape(8.0, 0.0, 0.0, 16.0, 8.0, 8.0)
        protected val BOTTOM_SOUTH_EAST_CORNER_SHAPE: VoxelShape = createCuboidShape(8.0, 0.0, 8.0, 16.0, 8.0, 16.0)
        protected val TOP_NORTH_EAST_CORNER_SHAPE: VoxelShape = createCuboidShape(8.0, 7.0, 0.0, 16.0, 15.0, 8.0)
        protected val TOP_SOUTH_EAST_CORNER_SHAPE: VoxelShape = createCuboidShape(8.0, 7.0, 8.0, 16.0, 15.0, 16.0)

        private val SHAPE_INDICES = intArrayOf(12, 5, 3, 10, 14, 13, 7, 11, 13, 7, 11, 14, 8, 4, 1, 2, 4, 1, 2, 8)

        protected val TOP_SHAPES: Array<VoxelShape> = composeShapes(
            TOP_SHAPE,
            BOTTOM_NORTH_WEST_CORNER_SHAPE,
            BOTTOM_NORTH_EAST_CORNER_SHAPE,
            BOTTOM_SOUTH_WEST_CORNER_SHAPE,
            BOTTOM_SOUTH_EAST_CORNER_SHAPE
        )

        protected val BOTTOM_SHAPES: Array<VoxelShape> = composeShapes(
            BOTTOM_SHAPE,
            TOP_NORTH_WEST_CORNER_SHAPE,
            TOP_NORTH_EAST_CORNER_SHAPE,
            TOP_SOUTH_WEST_CORNER_SHAPE,
            TOP_SOUTH_EAST_CORNER_SHAPE
        )

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

    private fun getShapeIndexIndex(state: BlockState): Int {
        return state.get(SHAPE).ordinal * 4 + state.get(FACING).horizontalQuarterTurns
    }

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView,
        pos: BlockPos,
        context: ShapeContext
    ): VoxelShape {
        return (if (state.get(HALF) == BlockHalf.TOP) TOP_SHAPES else BOTTOM_SHAPES)[SHAPE_INDICES[getShapeIndexIndex(state)]]
    }
}