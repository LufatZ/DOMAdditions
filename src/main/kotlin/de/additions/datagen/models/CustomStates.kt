@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen.models

import de.additions.datagen.models.CustomStates.StairsModelType.*
import net.minecraft.block.Block
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.SlabType
import net.minecraft.block.enums.StairShape
import net.minecraft.client.data.BlockModelDefinitionCreator
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.BlockStateVariantMap
import net.minecraft.client.data.VariantsBlockModelDefinitionCreator
import net.minecraft.client.render.model.json.WeightedVariant
import net.minecraft.state.property.Properties
import net.minecraft.util.math.Direction

object CustomStates {
    /**
     * Enum to represent all possible stair model types
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
     * Helper function to create a model map from individual models
     */
    fun createStairsModelMap(
        innerModel: WeightedVariant,
        regularModel: WeightedVariant,
        outerModel: WeightedVariant,
        innerModelRotated: WeightedVariant,
        regularModelRotated: WeightedVariant,
        outerModelRotated: WeightedVariant,
        innerSnowyModel: WeightedVariant?,
        regularSnowyModel: WeightedVariant?,
        outerSnowyModel: WeightedVariant?,
        innerSnowyModelRotated: WeightedVariant?,
        regularSnowyModelRotated: WeightedVariant?,
        outerSnowyModelRotated: WeightedVariant?,
    ): Map<StairsModelType, WeightedVariant?> =
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
     * Creates a BlockStateSupplier for snowy stairs blocks with all possible variants.
     *
     * This function generates all possible blockstate variants for a stairs block that can be covered in snow.
     * It handles different stair shapes (straight, inner, outer), positions (top/bottom), and directions (N/S/E/W).
     *
     * @param stairsBlock The stairs block to create variants for
     * @param models Map containing all needed model variants
     * @return BlockModelDefinitionCreator containing all possible variants
     */
    fun createNewSnowyStairsBlockState(
        stairsBlock: Block,
        models: Map<StairsModelType, WeightedVariant?>,
    ): BlockModelDefinitionCreator =
        VariantsBlockModelDefinitionCreator.of(stairsBlock).with(
            BlockStateVariantMap
                .models<Direction?, BlockHalf?, StairShape?, Boolean?>(
                    Properties.HORIZONTAL_FACING,
                    Properties.BLOCK_HALF,
                    Properties.STAIR_SHAPE,
                    Properties.SNOWY,
                ).apply {
                    val facing =
                        mapOf(
                            Direction.EAST to 0,
                            Direction.WEST to 180,
                            Direction.SOUTH to 90,
                            Direction.NORTH to 270,
                        )

                    for ((direction, rotation) in facing) {
                        for (half in listOf(BlockHalf.BOTTOM, BlockHalf.TOP)) {
                            for (shape in StairShape.entries) {
                                for (isSnowy in listOf(false, true)) {
                                    // Wähle das entsprechende Modell basierend auf Form und Schnee-Status
                                    val isOuter = shape == StairShape.OUTER_LEFT || shape == StairShape.OUTER_RIGHT
                                    val isInner = shape == StairShape.INNER_LEFT || shape == StairShape.INNER_RIGHT
                                    val isTop = half == BlockHalf.TOP
                                    var modelType =
                                        when {
                                            !isTop && shape == StairShape.STRAIGHT && !isSnowy -> REGULAR
                                            !isTop && shape == StairShape.STRAIGHT && isSnowy -> REGULAR_SNOWY
                                            isTop && shape == StairShape.STRAIGHT && !isSnowy -> REGULAR_ROTATED
                                            isTop && shape == StairShape.STRAIGHT && isSnowy -> REGULAR_SNOWY_ROTATED
                                            !isTop && isOuter && !isSnowy -> OUTER
                                            !isTop && isOuter && isSnowy -> OUTER_SNOWY
                                            isTop && isOuter && !isSnowy -> OUTER_ROTATED
                                            isTop && isOuter && isSnowy -> OUTER_SNOWY_ROTATED
                                            !isTop && isInner && !isSnowy -> INNER
                                            !isTop && isInner && isSnowy -> INNER_SNOWY
                                            isTop && isInner && !isSnowy -> INNER_ROTATED
                                            isTop && isInner && isSnowy -> INNER_SNOWY_ROTATED
                                            else -> REGULAR
                                        }

                                    var model = models[modelType] ?: models[REGULAR]!!

                                    if (half == BlockHalf.TOP) {
                                        model = model.apply(BlockStateModelGenerator.ROTATE_X_180)
                                    }

                                    var rot = rotation

                                    // Bestimme zusätzlichen Rotationsoffset für bestimmte Formen
                                    rot += if (shape == StairShape.OUTER_LEFT || shape == StairShape.INNER_LEFT) 270 else 0
                                    rot += if (isTop && shape != StairShape.STRAIGHT) 90 else 0

                                    // Wende die Y-Rotation an
                                    model =
                                        when (rot % 360) {
                                            90 -> model.apply(BlockStateModelGenerator.ROTATE_Y_90)
                                            180 -> model.apply(BlockStateModelGenerator.ROTATE_Y_180)
                                            270 -> model.apply(BlockStateModelGenerator.ROTATE_Y_270)
                                            else -> model
                                        }
                                    model = model.apply(BlockStateModelGenerator.UV_LOCK)
                                    register(direction, half, shape, isSnowy, model)
                                }
                            }
                        }
                    }
                },
        )

    /**
     * Creates a BlockStateSupplier for overgrown stairs blocks with all possible variants.
     *
     * This function generates all possible blockstate variants for a stairs block that can be overgrown.
     * It handles different stair shapes (straight, inner, outer), positions (top/bottom), and directions (N/S/E/W).
     *
     * @param stairsBlock The stairs block to create variants for
     * @param innerModel Model for inner corner variant
     * @param regularModel Model for straight variant
     * @param outerModel Model for outer corner variant
     * @param innerModelRotated Model for rotated inner corner variant (top half)
     * @param regularModelRotated Model for rotated straight variant (top half)
     * @param outerModelRotated Model for rotated outer corner variant (top half)
     * @return BlockStateSupplier containing all possible variants
     */
    fun createNewOvergrownStairsBlockState(
        stairsBlock: Block,
        innerModel: WeightedVariant,
        regularModel: WeightedVariant,
        outerModel: WeightedVariant,
        innerModelRotated: WeightedVariant,
        regularModelRotated: WeightedVariant,
        outerModelRotated: WeightedVariant,
    ): BlockModelDefinitionCreator =
        VariantsBlockModelDefinitionCreator.of(stairsBlock).with(
            BlockStateVariantMap
                .models<Direction?, BlockHalf?, StairShape?>(
                    Properties.HORIZONTAL_FACING,
                    Properties.BLOCK_HALF,
                    Properties.STAIR_SHAPE,
                ).apply {
                    val rotations =
                        mapOf(
                            Direction.EAST to 0,
                            Direction.WEST to 180,
                            Direction.SOUTH to 90,
                            Direction.NORTH to 270,
                        )

                    for ((direction, yRot) in rotations) {
                        for (half in listOf(BlockHalf.BOTTOM, BlockHalf.TOP)) {
                            for (shape in listOf(
                                StairShape.STRAIGHT,
                                StairShape.OUTER_RIGHT,
                                StairShape.OUTER_LEFT,
                                StairShape.INNER_RIGHT,
                                StairShape.INNER_LEFT,
                            )) {
                                // Wähle das entsprechende Modell basierend auf Form
                                val model =
                                    when (shape) {
                                        StairShape.STRAIGHT -> regularModel
                                        StairShape.OUTER_LEFT, StairShape.OUTER_RIGHT -> outerModel
                                        else -> innerModel // für INNER_LEFT und INNER_RIGHT
                                    }

                                // Wähle das entsprechende rotierte Modell für bestimmte Fälle
                                val modelToUse =
                                    when {
                                        direction == Direction.NORTH && shape == StairShape.STRAIGHT -> regularModelRotated
                                        direction == Direction.NORTH &&
                                            (shape == StairShape.OUTER_LEFT || shape == StairShape.OUTER_RIGHT) -> outerModelRotated

                                        direction == Direction.NORTH -> innerModelRotated
                                        else -> model
                                    }

                                // Bestimme zusätzlichen Rotationsoffset für bestimmte Formen
                                val shapeRotOffset =
                                    when (shape) {
                                        StairShape.OUTER_LEFT, StairShape.INNER_LEFT -> -90
                                        else -> 0
                                    }

                                val effectiveYRot = (yRot + shapeRotOffset) % 360

                                // Transformationen anwenden: Zuerst ggf. X-Rotation, dann Y-Rotation und schließlich UV_LOCK
                                var transformedModel = modelToUse
                                if (half == BlockHalf.TOP) {
                                    transformedModel = transformedModel.apply(BlockStateModelGenerator.ROTATE_X_180)
                                }
                                if (effectiveYRot != 0) {
                                    transformedModel =
                                        when (effectiveYRot) {
                                            90 -> transformedModel.apply(BlockStateModelGenerator.ROTATE_Y_90)
                                            180 -> transformedModel.apply(BlockStateModelGenerator.ROTATE_Y_180)
                                            270 -> transformedModel.apply(BlockStateModelGenerator.ROTATE_Y_270)
                                            else -> transformedModel
                                        }
                                }
                                transformedModel = transformedModel.apply(BlockStateModelGenerator.UV_LOCK)

                                register(direction, half, shape, transformedModel)
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
     * @param bottomModel Model bottom variant
     * @param topModel Model for top variant
     * @param fullModel Model for full block variant
     * @param bottomSnowyModel Model for snowy bottom variant
     * @param topSnowyModel Model for snowy top variant
     * @param fullSnowyModel Model for snowy full block variant
     * @return BlockStateSupplier containing all possible variants
     */
    fun createNewSnowySlabBlockState(
        slabBlock: Block,
        bottomModel: WeightedVariant,
        topModel: WeightedVariant,
        fullModel: WeightedVariant,
        bottomSnowyModel: WeightedVariant,
        topSnowyModel: WeightedVariant,
        fullSnowyModel: WeightedVariant,
    ): BlockModelDefinitionCreator =
        VariantsBlockModelDefinitionCreator.of(slabBlock).with(
            BlockStateVariantMap
                .models<SlabType, Boolean>(
                    Properties.SLAB_TYPE,
                    Properties.SNOWY,
                ).apply {
                    // Registrierung der Modelle für nicht verschneite Slabs
                    register(SlabType.BOTTOM, false, bottomModel)
                    register(SlabType.TOP, false, topModel)
                    register(SlabType.DOUBLE, false, fullModel)
                    // Registrierung der Modelle für verschneite Slabs
                    register(SlabType.BOTTOM, true, bottomSnowyModel)
                    register(SlabType.TOP, true, topSnowyModel)
                    register(SlabType.DOUBLE, true, fullSnowyModel)
                },
        )
}
