@file:Suppress("ktlint:standard:no-wildcard-imports") // Keep your suppress if needed

package de.additions.datagen.models // Adjust package as needed

import de.additions.datagen.models.CustomStates.createCustomStairsBlockState
import de.additions.datagen.models.CustomStates.createStairsModelIdMap
import net.minecraft.block.Block
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.SlabType
import net.minecraft.block.enums.StairShape
import net.minecraft.client.data.*
import net.minecraft.state.property.BooleanProperty
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import java.util.*

/**
 * Utility object for generating custom block states for stairs and slabs
 * with special properties like snow coverage.
 * This code mirrors the structure of a newer version (e.g., 1.21.5) but is adapted
 * internally to be compatible with older mechanisms (e.g., 1.21.4 BlockStateModelGenerator)
 * by using standard `.put()` methods for variant configuration.
 */
object CustomStates {
    // --- Nested Helper Enum for Transformation Simulation (If needed, but simplified direct .put is used now) ---
    // Kept internal logic simpler by directly using .put in applyDefaultStairRotation

    // --- Public API mirroring the 1.21.5 structure ---

    /**
     * Enum representing conceptual stair model types used for organizing input models.
     * Mirrors the structure from the newer code version. Used as keys in the input map.
     */
    enum class StairsModelType {
        INNER,
        REGULAR,
        OUTER,
        INNER_ROTATED,
        REGULAR_ROTATED,
        OUTER_ROTATED, // Represent base models for top stairs
        INNER_SNOWY,
        REGULAR_SNOWY,
        OUTER_SNOWY,
        INNER_SNOWY_ROTATED,
        REGULAR_SNOWY_ROTATED,
        OUTER_SNOWY_ROTATED, // Represent snowy models for top stairs
    }

    /**
     * Mapping of facing directions to their corresponding base rotation values in degrees.
     */
    private val facingRotations =
        mapOf(
            Direction.EAST to 0,
            Direction.SOUTH to 90,
            Direction.WEST to 180,
            Direction.NORTH to 270,
        )

    /**
     * Determines if a stair shape is an inner stair type.
     * @param shape The stair shape to check.
     * @return True if the shape is an inner stair (left or right).
     */
    private fun isInnerStair(shape: StairShape): Boolean = shape == StairShape.INNER_LEFT || shape == StairShape.INNER_RIGHT

    /**
     * Determines if a stair shape is an outer stair type.
     * @param shape The stair shape to check.
     * @return True if the shape is an outer stair (left or right).
     */
    private fun isOuterStair(shape: StairShape): Boolean = shape == StairShape.OUTER_LEFT || shape == StairShape.OUTER_RIGHT

    /**
     * Determines if a stair half is in the top position.
     * @param half The block half to check.
     * @return True if the half is the top half.
     */
    private fun isTopStair(half: BlockHalf): Boolean = half == BlockHalf.TOP

    /**
     * Creates a map associating stair model types with their corresponding model Identifiers.
     * This helper function constructs the map needed by [createCustomStairsBlockState].
     * Nullable Identifiers are allowed for optional models (e.g., snowy).
     *
     * @param innerModelId Identifier for inner stairs model.
     * @param regularModelId Identifier for regular stairs model.
     * @param outerModelId Identifier for outer stairs model.
     * @param innerModelRotatedId Identifier for top inner stairs model.
     * @param regularModelRotatedId Identifier for top regular stairs model.
     * @param outerModelRotatedId Identifier for top outer stairs model.
     * @param innerSnowyModelId Optional Identifier for snowy inner stairs.
     * @param regularSnowyModelId Optional Identifier for snowy regular stairs.
     * @param outerSnowyModelId Optional Identifier for snowy outer stairs.
     * @param innerSnowyModelRotatedId Optional Identifier for snowy top inner stairs.
     * @param regularSnowyModelRotatedId Optional Identifier for snowy top regular stairs.
     * @param outerSnowyModelRotatedId Optional Identifier for snowy top outer stairs.
     * @return A map associating each stair model type with its corresponding Identifier (nullable).
     */
    fun createStairsModelIdMap( // Renamed from createStairsModelMap used in BlockModels.kt context
        innerModelId: Identifier,
        regularModelId: Identifier,
        outerModelId: Identifier,
        innerModelRotatedId: Identifier,
        regularModelRotatedId: Identifier,
        outerModelRotatedId: Identifier,
        innerSnowyModelId: Identifier? = null,
        regularSnowyModelId: Identifier? = null,
        outerSnowyModelId: Identifier? = null,
        innerSnowyModelRotatedId: Identifier? = null,
        regularSnowyModelRotatedId: Identifier? = null,
        outerSnowyModelRotatedId: Identifier? = null,
    ): Map<StairsModelType, Identifier?> {
        val map = EnumMap<StairsModelType, Identifier>(StairsModelType::class.java)
        map[StairsModelType.INNER] = innerModelId
        map[StairsModelType.REGULAR] = regularModelId
        map[StairsModelType.OUTER] = outerModelId
        map[StairsModelType.INNER_ROTATED] = innerModelRotatedId
        map[StairsModelType.REGULAR_ROTATED] = regularModelRotatedId
        map[StairsModelType.OUTER_ROTATED] = outerModelRotatedId
        innerSnowyModelId?.let { map[StairsModelType.INNER_SNOWY] = it }
        regularSnowyModelId?.let { map[StairsModelType.REGULAR_SNOWY] = it }
        outerSnowyModelId?.let { map[StairsModelType.OUTER_SNOWY] = it }
        innerSnowyModelRotatedId?.let { map[StairsModelType.INNER_SNOWY_ROTATED] = it }
        regularSnowyModelRotatedId?.let { map[StairsModelType.REGULAR_SNOWY_ROTATED] = it }
        outerSnowyModelRotatedId?.let { map[StairsModelType.OUTER_SNOWY_ROTATED] = it }
        return map
    }

    /**
     * Selects the appropriate model Identifier from the map based on stair properties.
     * Mirrors the logic of the newer `getModelType` function but returns an Identifier.
     * Handles fallbacks if specific (e.g., snowy) variants are missing.
     *
     * @param modelIds Map of available model identifiers (nullable values allowed).
     * @param half The block half (top or bottom).
     * @param shape The stair shape.
     * @param isSnowy Whether the stair is covered in snow.
     * @return The appropriate model Identifier.
     * @throws IllegalStateException if the fallback REGULAR model identifier is missing.
     */
    private fun getModelId(
        modelIds: Map<StairsModelType, Identifier?>,
        half: BlockHalf,
        shape: StairShape,
        isSnowy: Boolean = false,
    ): Identifier {
        val isTop = isTopStair(half)
        val modelType =
            when {
                !isTop && shape == StairShape.STRAIGHT && !isSnowy -> StairsModelType.REGULAR
                !isTop && shape == StairShape.STRAIGHT && isSnowy -> StairsModelType.REGULAR_SNOWY
                isTop && shape == StairShape.STRAIGHT && !isSnowy -> StairsModelType.REGULAR_ROTATED
                isTop && shape == StairShape.STRAIGHT && isSnowy -> StairsModelType.REGULAR_SNOWY_ROTATED
                !isTop && isOuterStair(shape) && !isSnowy -> StairsModelType.OUTER
                !isTop && isOuterStair(shape) && isSnowy -> StairsModelType.OUTER_SNOWY
                isTop && isOuterStair(shape) && !isSnowy -> StairsModelType.OUTER_ROTATED
                isTop && isOuterStair(shape) && isSnowy -> StairsModelType.OUTER_SNOWY_ROTATED
                !isTop && isInnerStair(shape) && !isSnowy -> StairsModelType.INNER
                !isTop && isInnerStair(shape) && isSnowy -> StairsModelType.INNER_SNOWY
                isTop && isInnerStair(shape) && !isSnowy -> StairsModelType.INNER_ROTATED
                isTop && isInnerStair(shape) && isSnowy -> StairsModelType.INNER_SNOWY_ROTATED
                else -> StairsModelType.REGULAR // Default fallback type
            }

        var identifier = modelIds[modelType]

        // Fallback logic if the selected identifier is null
        if (identifier == null) {
            if (isSnowy || modelType.name.contains("SNOWY") || modelType.name.contains("ROTATED")) {
                val fallbackType =
                    when (modelType) {
                        StairsModelType.REGULAR_SNOWY, StairsModelType.REGULAR_SNOWY_ROTATED -> StairsModelType.REGULAR
                        StairsModelType.OUTER_SNOWY, StairsModelType.OUTER_SNOWY_ROTATED -> StairsModelType.OUTER
                        StairsModelType.INNER_SNOWY, StairsModelType.INNER_SNOWY_ROTATED -> StairsModelType.INNER
                        StairsModelType.REGULAR_ROTATED -> StairsModelType.REGULAR
                        StairsModelType.OUTER_ROTATED -> StairsModelType.OUTER
                        StairsModelType.INNER_ROTATED -> StairsModelType.INNER
                        else -> StairsModelType.REGULAR
                    }
                System.err.println("Warning: Model identifier for type $modelType not found. Falling back to $fallbackType.")
                identifier = modelIds[fallbackType]
            }
        }

        // Final check, ensure REGULAR exists
        return identifier ?: modelIds[StairsModelType.REGULAR]
            ?: throw IllegalStateException("REGULAR stair model identifier must be provided in the map!")
    }

    /**
     * Applies calculated rotations and UV lock directly to a BlockStateVariant using `.put()`.
     * This replaces the incompatible `.apply(TRANSFORMATION)` logic from the newer code version,
     * while preserving the core rotation calculation logic. Uses standard 1.21.4 mechanisms.
     *
     * @param variant The BlockStateVariant to modify (should initially contain the model ID).
     * @param half The block half (TOP or BOTTOM).
     * @param shape The stair shape (STRAIGHT, INNER_LEFT, etc.).
     * @param rotation The base Y rotation based on the facing direction.
     * @return The modified BlockStateVariant with transformations applied.
     */
    private fun applyDefaultStairRotation(
        variant: BlockStateVariant, // Takes and modifies variant directly
        half: BlockHalf,
        shape: StairShape,
        rotation: Int,
    ): BlockStateVariant {
        var rot = rotation // Local var for accumulated Y rotation

        // Apply X rotation for top stairs directly using .put
        if (isTopStair(half)) {
            variant.put(VariantSettings.X, VariantSettings.Rotation.R180)
        }

        // Apply Y rotation offsets based on shape and half (User's specific logic)
        rot += if (shape == StairShape.OUTER_LEFT || shape == StairShape.INNER_LEFT) 270 else 0
        rot += if (isTopStair(half) && shape != StairShape.STRAIGHT) 90 else 0

        // Apply final accumulated Y rotation directly using .put
        when (rot % 360) {
            90 -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R90)
            180 -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R180)
            270 -> variant.put(VariantSettings.Y, VariantSettings.Rotation.R270)
            // case 0: no Y rotation needed
        }

        // Apply UV lock directly using .put
        // Apply UVLock unconditionally as per original 1.21.4 logic's createVariant helper
        variant.put(VariantSettings.UVLOCK, true) // Apply UVLock

        return variant // Return the modified variant
    }

    /**
     * Creates a BlockStateSupplier for custom stairs blocks.
     * Delegates based on the provided property (e.g., SNOWY).
     * The calling code is responsible for creating the `modelIds` map containing the
     * necessary model Identifiers for each [StairsModelType] (e.g., using [createStairsModelIdMap]).
     *
     * @param stairsBlock The stairs block instance.
     * @param modelIds A map from StairsModelType to model Identifier (nullable values allowed for optional models).
     * @param property The optional BooleanProperty, e.g., Properties.SNOWY, to switch behavior.
     * @return A BlockStateSupplier defining the block states.
     */
    fun createCustomStairsBlockState(
        stairsBlock: Block,
        modelIds: Map<StairsModelType, Identifier?>,
        property: BooleanProperty? = null,
    ): BlockStateSupplier =
        if (property == Properties.SNOWY) {
            createSnowyStairsBlockStateInternal(stairsBlock, modelIds)
        } else {
            createOvergrownStairsBlockStateInternal(stairsBlock, modelIds)
        }

    /**
     * Internal implementation for generating states for stairs without the SNOWY property check.
     */
    private fun createOvergrownStairsBlockStateInternal(
        stairsBlock: Block,
        modelIds: Map<StairsModelType, Identifier?>,
    ): BlockStateSupplier =
        VariantsBlockStateSupplier
            .create(stairsBlock)
            .coordinate(
                BlockStateVariantMap
                    .create(
                        Properties.HORIZONTAL_FACING,
                        Properties.BLOCK_HALF,
                        Properties.STAIR_SHAPE,
                    ).register { facing, half, shape ->
                        // Select the base model ID (non-snowy)
                        val baseModelId = getModelId(modelIds, half, shape, false)
                        val baseRotation = facingRotations[facing] ?: 0

                        // Create a new variant with the model ID
                        val variant = BlockStateVariant.create().put(VariantSettings.MODEL, baseModelId)

                        // Apply rotations directly to the new variant
                        applyDefaultStairRotation(variant, half, shape, baseRotation)
                    },
            )

    /**
     * Internal implementation for generating states for stairs WITH the SNOWY property check.
     */
    private fun createSnowyStairsBlockStateInternal(
        stairsBlock: Block,
        modelIds: Map<StairsModelType, Identifier?>,
    ): BlockStateSupplier =
        VariantsBlockStateSupplier
            .create(stairsBlock)
            .coordinate(
                BlockStateVariantMap
                    .create(
                        Properties.HORIZONTAL_FACING,
                        Properties.BLOCK_HALF,
                        Properties.STAIR_SHAPE,
                        Properties.SNOWY,
                    ).register { facing, half, shape, snowy ->
                        // Select the base model ID (snowy or non-snowy)
                        val baseModelId = getModelId(modelIds, half, shape, snowy)
                        val baseRotation = facingRotations[facing] ?: 0

                        // Create a new variant with the model ID
                        val variant = BlockStateVariant.create().put(VariantSettings.MODEL, baseModelId)

                        // Apply rotations directly to the new variant
                        applyDefaultStairRotation(variant, half, shape, baseRotation)
                    },
            )

    /**
     * Creates a BlockStateSupplier for slab blocks potentially with a SNOWY property.
     * Directly maps states to provided model Identifiers.
     *
     * @param slabBlock The slab block instance.
     * @param bottomModelId Identifier for the bottom slab model.
     * @param topModelId Identifier for the top slab model.
     * @param fullModelId Identifier for the double slab (full block) model.
     * @param bottomSnowyModelId Optional Identifier for the snowy bottom slab.
     * @param topSnowyModelId Optional Identifier for the snowy top slab.
     * @param fullSnowyModelId Optional Identifier for the snowy double slab.
     * @return A BlockStateSupplier defining the slab states.
     */
    fun createSnowySlabBlockState(
        slabBlock: Block,
        bottomModelId: Identifier,
        topModelId: Identifier,
        fullModelId: Identifier,
        bottomSnowyModelId: Identifier? = null, // Use nullable for optional snowy models
        topSnowyModelId: Identifier? = null,
        fullSnowyModelId: Identifier? = null,
    ): BlockStateSupplier =
        VariantsBlockStateSupplier
            .create(slabBlock)
            .coordinate(
                BlockStateVariantMap
                    .create(Properties.SLAB_TYPE, Properties.SNOWY)
                    .register { slabType, snowy ->
                        val modelId =
                            when (slabType) {
                                SlabType.BOTTOM -> if (snowy) bottomSnowyModelId ?: bottomModelId else bottomModelId
                                SlabType.TOP -> if (snowy) topSnowyModelId ?: topModelId else topModelId
                                SlabType.DOUBLE -> if (snowy) fullSnowyModelId ?: fullModelId else fullModelId
                            }
                        // Create variant directly with the selected model ID
                        BlockStateVariant.create().put(
                            VariantSettings.MODEL,
                            modelId
                                ?: throw IllegalStateException("Fallback model identifier is missing for SlabType $slabType, snowy=$snowy"),
                        )
                    },
            )
}
