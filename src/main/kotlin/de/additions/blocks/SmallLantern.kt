package de.additions.blocks

import net.minecraft.block.BlockState
import net.minecraft.block.LanternBlock
import net.minecraft.block.ShapeContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView

/**
 * Represents a smaller version of a lantern block.
 * This lantern has a custom, more detailed shape compared to the default lantern.
 *
 * @param settings The settings for the block.
 */
class SmallLantern(
    settings: Settings?,
) : LanternBlock(settings) {
    companion object {
        /** The outline shape for a hanging small lantern. */
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
        /** The outline shape for a standing small lantern. */
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

    /**
     * Gets the outline shape of the lantern based on its state.
     * Overridden to provide the custom small lantern shapes.
     */
    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?,
    ): VoxelShape = if (state.get(HANGING)) SMALL_HANGING_SHAPE else SMALL_STANDING_SHAPE
}
