package de.additions.blocks.lanterns

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.LanternBlock
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter

/**
 * Represents a larger version of a lantern block.
 * This lantern has a custom, more detailed shape compared to the default lantern.
 *
 * @param settings The settings for the block.
 */
open class BigLantern(
    settings: Properties,
) : LanternBlock(settings) {
    companion object {
        /** The outline shape for a hanging big lantern. */
        val BIG_HANGING_SHAPE: VoxelShape =
            Shapes.or(
                box(5.0, 11.0, 5.0, 11.0, 12.0, 11.0),
                box(4.0, 10.0, 4.0, 12.0, 11.0, 12.0),
                box(3.0, 8.0, 3.0, 13.0, 10.0, 13.0),
                box(5.0, 2.0, 5.0, 11.0, 8.0, 11.0),
                box(3.0, 2.0, 12.0, 4.0, 8.0, 13.0),
                box(3.0, 2.0, 3.0, 4.0, 8.0, 4.0),
                box(12.0, 2.0, 3.0, 13.0, 8.0, 4.0),
                box(12.0, 2.0, 12.0, 13.0, 8.0, 13.0),
                box(3.0, 1.0, 3.0, 13.0, 2.0, 13.0),
                box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            )
        /** The outline shape for a standing big lantern. */
        val BIG_STANDING_SHAPE: VoxelShape =
            Shapes.or(
                box(5.0, 11.0, 5.0, 11.0, 12.0, 11.0),
                box(4.0, 10.0, 4.0, 12.0, 11.0, 12.0),
                box(3.0, 8.0, 3.0, 13.0, 10.0, 13.0),
                box(5.0, 2.0, 5.0, 11.0, 8.0, 11.0),
                box(3.0, 2.0, 12.0, 4.0, 8.0, 13.0),
                box(3.0, 2.0, 3.0, 4.0, 8.0, 4.0),
                box(12.0, 2.0, 3.0, 13.0, 8.0, 4.0),
                box(12.0, 2.0, 12.0, 13.0, 8.0, 13.0),
                box(3.0, 1.0, 3.0, 13.0, 2.0, 13.0),
                box(4.0, 0.0, 4.0, 12.0, 1.0, 12.0),
            )
    }

    /**
     * Gets the outline shape of the lantern based on its state.
     * Overridden to provide the custom big lantern shapes.
     */
    override fun getShape(
        state: BlockState,
        blockGetter: BlockGetter,
        blockPos: BlockPos,
        collisionContext: CollisionContext
    ): VoxelShape = if (state.getValue(HANGING)) BIG_HANGING_SHAPE else BIG_STANDING_SHAPE
}