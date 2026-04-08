package de.additions.helper

import de.additions.blocks.BlockRegistry
import de.additions.datagen.BlockTagGenerator
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.state.BlockState

/**
 * Utility object for handling block state conversions related to path blocks and entity position adjustments.
 */
object PathConversionHelper {

    /**
     * Determines the appropriate target block state for a dirt path variant.
     * If the source block is already part of the dirt path variant tag, it is returned unchanged.
     * Otherwise, it converts stair or slab blocks into their respective dirt path equivalents,
     * or defaults to the standard dirt path block state.
     *
     * @param source The source block state to evaluate.
     * @return The resulting block state mapped to a dirt path variant or the original state if already a variant.
     */
    @JvmStatic
    fun getPathTargetState(source: BlockState): BlockState {
        if (source.`is`(BlockTagGenerator.DirtPathVariantTag)) return source

        return when (source.block) {
            is StairBlock -> BlockRegistry.DIRT_PATH_STAIR.withPropertiesOf(source)
            is SlabBlock -> BlockRegistry.DIRT_PATH_SLAB.withPropertiesOf(source)
            else -> Blocks.DIRT_PATH.defaultBlockState()
        }
    }

    /**
     * Reverts a given block state to its corresponding dirt-based state.
     * If the source is a stair or slab, it returns the respective dirt variant with preserved properties;
     * otherwise, it returns the default dirt block state.
     *
     * @param source The original block state to be reverted.
     * @return A new block state representing the dirt version of the input block.
     */
    @JvmStatic
    fun revertToDirtState(source: BlockState): BlockState = when (source.block) {
        is StairBlock -> BlockRegistry.DIRT_STAIR.withPropertiesOf(source)
        is SlabBlock -> BlockRegistry.DIRT_SLAB.withPropertiesOf(source)
        else -> Blocks.DIRT.withPropertiesOf(source)
    }

    /**
     * Checks whether a given block state represents a pathable surface, such as dirt,
     * dirt-like blocks, or grass blocks.
     *
     * @param state The block state to check for pathability.
     * @return True if the block state matches any of the defined pathable tags, false otherwise.
     */
    @JvmStatic
    fun isPathable(state: BlockState): Boolean {
        return state.`is`(BlockTags.DIRT) ||
                state.`is`(BlockTagGenerator.DirtLikeBlockTag) ||
                state.`is`(BlockTags.GRASS_BLOCKS)
    }

    /**
     * Adjusts the vertical position of entities located within the bounds of a specific block state.
     *
     * Iterates through all entities found in a detection box slightly offset above the block's shape.
     * If an entity is found to be below the top surface of the block, its Y-coordinate is updated
     * to match the top boundary of the block's shape.
     *
     * @param world The level where the entities are located.
     * @param pos The position of the block being checked.
     * @param state The block state used to determine the collision shape and top surface height.
     */
    @JvmStatic
    fun applyEntityFix(world: Level, pos: BlockPos, state: BlockState) {
        val shape = state.getShape(world, pos)
        val topY = pos.y.toDouble() + if (shape.isEmpty) 1.0 else shape.max(Direction.Axis.Y)

        val detectionBox = shape.bounds().move(pos).inflate(0.0, 0.1, 0.0)
        val entities = world.getEntities(null, detectionBox)

        for (entity in entities) {
            if (entity.position().y < topY) {
                entity.setPos(entity.x, topY, entity.z)
            }
        }
    }
}
