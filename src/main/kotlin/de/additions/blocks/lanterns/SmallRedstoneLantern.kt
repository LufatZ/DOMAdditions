package de.additions.blocks.lanterns

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter

/**
 * Represents a smaller version of a redstone-powered lantern block.
 * This lantern has a custom, more detailed shape and lights up when powered by redstone.
 *
 * @param settings The settings for the block.
 */
open class SmallRedstoneLantern(
    settings: Properties,
) : RedstoneLantern(settings) {
    companion object {
        /** The outline shape for a hanging small redstone lantern. */
        val SMALL_HANGING_SHAPE: VoxelShape =
            Shapes.or(
                box(7.0, 3.0, 7.0, 9.0, 6.0, 9.0),
                box(7.0, 2.0, 7.0, 9.0, 3.0, 9.0),
                box(7.0, 9.0, 7.0, 9.0, 10.0, 9.0),
                box(6.0, 0.0, 6.0, 10.0, 2.0, 10.0),
                box(7.0, 6.0, 7.0, 9.0, 8.0, 9.0),
                box(8.0, 2.0, 6.0, 9.0, 3.0, 7.0),
                box(7.0, 2.0, 9.0, 8.0, 3.0, 10.0),
                box(7.0, 6.0, 9.0, 8.0, 7.0, 10.0),
                box(8.0, 6.0, 6.0, 9.0, 7.0, 7.0),
                box(8.0, 3.0, 5.0, 9.0, 6.0, 6.0),
                box(7.0, 3.0, 10.0, 8.0, 6.0, 11.0),
                box(7.5, 8.0, 7.5, 8.5, 9.0, 8.5),
            )
        /** The outline shape for a standing small redstone lantern. */
        val SMALL_STANDING_SHAPE: VoxelShape =
            Shapes.or(
                box(7.0, 3.0, 7.0, 9.0, 6.0, 9.0),
                box(7.0, 2.0, 7.0, 9.0, 3.0, 9.0),
                box(7.0, 9.0, 7.0, 9.0, 10.0, 9.0),
                box(6.0, 0.0, 6.0, 10.0, 2.0, 10.0),
                box(7.0, 6.0, 7.0, 9.0, 8.0, 9.0),
                box(8.0, 2.0, 6.0, 9.0, 3.0, 7.0),
                box(7.0, 2.0, 9.0, 8.0, 3.0, 10.0),
                box(7.0, 6.0, 9.0, 8.0, 7.0, 10.0),
                box(8.0, 6.0, 6.0, 9.0, 7.0, 7.0),
                box(8.0, 3.0, 5.0, 9.0, 6.0, 6.0),
                box(7.0, 3.0, 10.0, 8.0, 6.0, 11.0),
                box(7.5, 8.0, 7.5, 8.5, 9.0, 8.5),
            )
    }

    /**
     * Gets the outline shape of the lantern based on its state.
     * Overridden to provide the custom small lantern shapes.
     */
    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext,
    ): VoxelShape = if (state.getValue(HANGING)) SMALL_HANGING_SHAPE else SMALL_STANDING_SHAPE
}