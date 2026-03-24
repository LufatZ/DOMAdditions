@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen.models

import de.additions.datagen.models.CustomStates.StairsModelType.*
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.Half
import net.minecraft.world.level.block.state.properties.SlabType
import net.minecraft.world.level.block.state.properties.StairsShape
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.blockstates.PropertyDispatch.initial
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.MultiVariant
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.Direction

/**
 * Utility object for generating custom block states for stairs and slabs
 * with special properties like snow coverage.
 */
object CustomStates {
    /**
     * Enum to represent all possible stair model types.
     * Includes variants for inner, regular, and outer stairs,
     * with options for rotation and snow coverage.
     */
    enum class StairsModelType {
        INNER,
        REGULAR,
        OUTER,
        INNER_ROTATED,
        REGULAR_ROTATED,
        OUTER_ROTATED,
        INNER_SNOWY,
        REGULAR_SNOWY,
        OUTER_SNOWY,
        INNER_SNOWY_ROTATED,
        REGULAR_SNOWY_ROTATED,
        OUTER_SNOWY_ROTATED,
    }

    /**
     * Mapping of facing directions to their corresponding rotation values in degrees.
     */
    private val facingRotations =
        mapOf(
            Direction.EAST to 0,
            Direction.WEST to 180,
            Direction.SOUTH to 90,
            Direction.NORTH to 270,
        )

    /**
     * Determines if a stair shape is an inner stair type.
     *
     * @param shape The stair shape to check
     * @return True if the shape is an inner stair (either left or right)
     */
    private fun isInnerStair(shape: StairsShape): Boolean = shape == StairsShape.INNER_LEFT || shape == StairsShape.INNER_RIGHT

    /**
     * Determines if a stair shape is an outer stair type.
     *
     * @param shape The stair shape to check
     * @return True if the shape is an outer stair (either left or right)
     */
    private fun isOuterStair(shape: StairsShape): Boolean = shape == StairsShape.OUTER_LEFT || shape == StairsShape.OUTER_RIGHT

    /**
     * Determines if a stair half is in the top position.
     *
     * @param half The block half to check
     * @return True if the half is the top half
     */
    private fun isTopStair(half: Half): Boolean = half == Half.TOP

    /**
     * Helper function to create a complete model map from individual model variants.
     *
     * @param innerModel Model for inner stairs
     * @param regularModel Model for regular (straight) stairs
     * @param outerModel Model for outer stairs
     * @param innerModelRotated Model for rotated inner stairs
     * @param regularModelRotated Model for rotated regular stairs
     * @param outerModelRotated Model for rotated outer stairs
     * @param innerSnowyModel Optional model for snowy inner stairs
     * @param regularSnowyModel Optional model for snowy regular stairs
     * @param outerSnowyModel Optional model for snowy outer stairs
     * @param innerSnowyModelRotated Optional model for rotated snowy inner stairs
     * @param regularSnowyModelRotated Optional model for rotated snowy regular stairs
     * @param outerSnowyModelRotated Optional model for rotated snowy outer stairs
     * @return A map associating each stair model type with its corresponding weighted variant
     */
    fun createStairsModelMap(
        innerModel: MultiVariant,
        regularModel: MultiVariant,
        outerModel: MultiVariant,
        innerModelRotated: MultiVariant,
        regularModelRotated: MultiVariant,
        outerModelRotated: MultiVariant,
        innerSnowyModel: MultiVariant? = null,
        regularSnowyModel: MultiVariant? = null,
        outerSnowyModel: MultiVariant? = null,
        innerSnowyModelRotated: MultiVariant? = null,
        regularSnowyModelRotated: MultiVariant? = null,
        outerSnowyModelRotated: MultiVariant? = null,
    ): Map<StairsModelType, MultiVariant?> =
        mapOf(
            INNER to innerModel,
            REGULAR to regularModel,
            OUTER to outerModel,
            INNER_ROTATED to innerModelRotated,
            REGULAR_ROTATED to regularModelRotated,
            OUTER_ROTATED to outerModelRotated,
            INNER_SNOWY to innerSnowyModel,
            REGULAR_SNOWY to regularSnowyModel,
            OUTER_SNOWY to outerSnowyModel,
            INNER_SNOWY_ROTATED to innerSnowyModelRotated,
            REGULAR_SNOWY_ROTATED to regularSnowyModelRotated,
            OUTER_SNOWY_ROTATED to outerSnowyModelRotated,
        )

    /**
     * Determines the appropriate model type based on stair properties.
     *
     * @param models Map of all available model variants
     * @param half The block half (top or bottom)
     * @param shape The stair shape
     * @param isSnowy Whether the stair is covered in snow
     * @return The appropriate weighted variant for the given properties
     */
    private fun getModelType(
        models: Map<StairsModelType, MultiVariant?>?,
        half: Half,
        shape: StairsShape,
        isSnowy: Boolean = false,
    ): MultiVariant {
        val isTop = isTopStair(half)
        val modelType =
            when {
                !isTop && shape == StairsShape.STRAIGHT && !isSnowy -> REGULAR
                !isTop && shape == StairsShape.STRAIGHT && isSnowy -> REGULAR_SNOWY
                isTop && shape == StairsShape.STRAIGHT && !isSnowy -> REGULAR_ROTATED
                isTop && shape == StairsShape.STRAIGHT && isSnowy -> REGULAR_SNOWY_ROTATED
                !isTop && isOuterStair(shape) && !isSnowy -> OUTER
                !isTop && isOuterStair(shape) && isSnowy -> OUTER_SNOWY
                isTop && isOuterStair(shape) && !isSnowy -> OUTER_ROTATED
                isTop && isOuterStair(shape) && isSnowy -> OUTER_SNOWY_ROTATED
                !isTop && isInnerStair(shape) && !isSnowy -> INNER
                !isTop && isInnerStair(shape) && isSnowy -> INNER_SNOWY
                isTop && isInnerStair(shape) && !isSnowy -> INNER_ROTATED
                isTop && isInnerStair(shape) && isSnowy -> INNER_SNOWY_ROTATED
                else -> REGULAR
            }

        return models?.get(modelType) ?: models?.get(REGULAR)!!
    }

    /**
     * Applies appropriate rotations to stair models based on their position and shape.
     *
     * @param inputModel The base model to apply rotations to
     * @param half The block half (top or bottom)
     * @param shape The stair shape
     * @param rotation The base rotation value
     * @return The model with all necessary rotations applied
     */
    private fun applyDefaultStairRotation(
        inputModel: MultiVariant,
        half: Half,
        shape: StairsShape,
        rotation: Int,
    ): MultiVariant {
        var model = inputModel
        var rot = rotation

        if (isTopStair(half)) {
            model = model.with(BlockModelGenerators.X_ROT_180)
        }

        // Determine additional rotation offset for specific shapes
        rot += if (shape == StairsShape.OUTER_LEFT || shape == StairsShape.INNER_LEFT) 270 else 0
        rot += if (isTopStair(half) && shape != StairsShape.STRAIGHT) 90 else 0

        // Apply Y-rotation
        model =
            when (rot % 360) {
                90 -> model.with(BlockModelGenerators.Y_ROT_90)
                180 -> model.with(BlockModelGenerators.Y_ROT_180)
                270 -> model.with(BlockModelGenerators.Y_ROT_270)
                else -> model
            }
        model = model.with(BlockModelGenerators.UV_LOCK)
        return model
    }

    /**
     * Creates a BlockStateSupplier for custom stairs blocks with all possible variants.
     * Delegates to the appropriate method based on the provided property.
     *
     * @param stairsBlock The stairs block to create variants for
     * @param models Map containing all needed model variants
     * @param property Optional boolean property to indicate special block state handling
     * @return BlockModelDefinitionCreator containing all possible variants
     */
    fun createCustomStairsBlockState(
        stairsBlock: Block,
        models: Map<StairsModelType, MultiVariant?>,
        property: BooleanProperty? = null,
    ): BlockModelDefinitionGenerator =
        if (property == BlockStateProperties.SNOWY) {
            createSnowyStairsBlockState(stairsBlock, models)
        } else {
            createOvergrownStairsBlockState(stairsBlock, models)
        }

    /**
     * Creates a BlockStateSupplier for overgrown stairs blocks with all possible variants.
     * This handles standard stair shapes and positions without snow coverage.
     *
     * @param stairsBlock The stairs block to create variants for
     * @param models Map containing all needed model variants
     * @return BlockModelDefinitionCreator containing all possible variants
     */
    private fun createOvergrownStairsBlockState(
        stairsBlock: Block,
        models: Map<StairsModelType, MultiVariant?>?,
    ): BlockModelDefinitionGenerator =
        MultiVariantGenerator.dispatch(stairsBlock).with(
            initial(
                BlockStateProperties.HORIZONTAL_FACING,
                BlockStateProperties.HALF,
                BlockStateProperties.STAIRS_SHAPE,
            ).apply {
                for ((direction, rotation) in facingRotations) {
                    for (half in listOf(Half.BOTTOM, Half.TOP)) {
                        for (shape in StairsShape.entries) {
                            // Select the appropriate model based on shape and half
                            var model =
                                getModelType(
                                    models,
                                    half,
                                    shape,
                                )
                            model =
                                applyDefaultStairRotation(
                                    model,
                                    half,
                                    shape,
                                    rotation,
                                )
                            select(direction, half, shape, model)
                        }
                    }
                }
            },
        )

    /**
     * Creates a BlockStateSupplier for snowy stairs blocks with all possible variants.
     * This handles different stair shapes, positions, and snow coverage states.
     *
     * @param stairsBlock The stairs block to create variants for
     * @param models Map containing all needed model variants
     * @return BlockModelDefinitionCreator containing all possible variants
     */
    private fun createSnowyStairsBlockState(
        stairsBlock: Block,
        models: Map<StairsModelType, MultiVariant?>?,
    ): BlockModelDefinitionGenerator =
        MultiVariantGenerator.dispatch(stairsBlock).with(
            initial(
                BlockStateProperties.HORIZONTAL_FACING,
                BlockStateProperties.HALF,
                BlockStateProperties.STAIRS_SHAPE,
                BlockStateProperties.SNOWY,
            ).apply {
                for ((direction, rotation) in facingRotations) {
                    for (half in listOf(Half.BOTTOM, Half.TOP)) {
                        for (shape in StairsShape.entries) {
                            for (isSnowy in listOf(false, true)) {
                                // Select the appropriate model based on shape and snow status
                                var model =
                                    getModelType(
                                        models,
                                        half,
                                        shape,
                                        isSnowy,
                                    )
                                model =
                                    applyDefaultStairRotation(
                                        model,
                                        half,
                                        shape,
                                        rotation,
                                    )
                                select(direction, half, shape, isSnowy, model)
                            }
                        }
                    }
                }
            },
        )

    /**
     * Creates a BlockStateSupplier for snowy slab blocks with all possible variants.
     *
     * This function generates all possible blockstate variants for a slab block that can be covered in snow.
     * It handles different slab properties (slabtype, snowy).
     *
     * @param slabBlock The slab block to create variants for
     * @param bottomModel Model for bottom variant
     * @param topModel Model for top variant
     * @param fullModel Model for full block variant
     * @param bottomSnowyModel Model for snowy bottom variant
     * @param topSnowyModel Model for snowy top variant
     * @param fullSnowyModel Model for snowy full block variant
     * @return BlockModelDefinitionCreator containing all possible variants
     */
    fun createSnowySlabBlockState(
        slabBlock: Block,
        bottomModel: MultiVariant,
        topModel: MultiVariant,
        fullModel: MultiVariant,
        bottomSnowyModel: MultiVariant,
        topSnowyModel: MultiVariant,
        fullSnowyModel: MultiVariant,
    ): BlockModelDefinitionGenerator =
        MultiVariantGenerator.dispatch(slabBlock).with(
            initial<SlabType, Boolean>(
                BlockStateProperties.SLAB_TYPE,
                BlockStateProperties.SNOWY,
            ).apply {
                // Register models for non-snowy slabs
                select(SlabType.BOTTOM, false, bottomModel)
                select(SlabType.TOP, false, topModel)
                select(SlabType.DOUBLE, false, fullModel)
                // Register models for snowy slabs
                select(SlabType.BOTTOM, true, bottomSnowyModel)
                select(SlabType.TOP, true, topSnowyModel)
                select(SlabType.DOUBLE, true, fullSnowyModel)
            },
        )
}
