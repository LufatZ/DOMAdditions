package de.additions.datagen.models

import net.minecraft.block.Block
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.SlabType
import net.minecraft.block.enums.StairShape
import net.minecraft.client.data.BlockStateSupplier
import net.minecraft.client.data.BlockStateVariant
import net.minecraft.client.data.BlockStateVariantMap
import net.minecraft.client.data.VariantSettings
import net.minecraft.client.data.VariantsBlockStateSupplier
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction

object CustomStates {
    /**
     * Creates a BlockStateSupplier for snowy stairs blocks with all possible variants.
     *
     * This function generates all possible blockstate variants for a stairs block that can be covered in snow.
     * It handles different stair shapes (straight, inner, outer), positions (top/bottom), and directions (N/S/E/W).
     *
     * @param stairsBlock The stairs block to create variants for
     * @param innerModelId Model for inner corner variant
     * @param regularModelId Model for straight variant
     * @param outerModelId Model for outer corner variant
     * @param innerModelRotated Model for rotated inner corner variant (top half)
     * @param defaultModelRotated Model for rotated straight variant (top half)
     * @param outerModelRotated Model for rotated outer corner variant (top half)
     * @param snowyInnerModel Model for snowy inner corner variant
     * @param snowyDefaultModel Model for snowy straight variant
     * @param snowyOuterModel Model for snowy outer corner variant
     * @param snowyInnerModelRotated Model for snowy rotated inner corner variant (top half)
     * @param snowyDefaultModelRotated Model for snowy rotated straight variant (top half)
     * @param snowyOuterModelRotated Model for snowy rotated outer corner variant (top half)
     * @return BlockStateSupplier containing all possible variants
     */
    fun createSnowyStairsBlockState(
        stairsBlock: Block,
        innerModelId: Identifier,
        regularModelId: Identifier,
        outerModelId: Identifier,
        innerModelRotated: Identifier?,
        defaultModelRotated: Identifier?,
        outerModelRotated: Identifier?,
        snowyInnerModel: Identifier?,
        snowyDefaultModel: Identifier?,
        snowyOuterModel: Identifier?,
        snowyInnerModelRotated: Identifier?,
        snowyDefaultModelRotated: Identifier?,
        snowyOuterModelRotated: Identifier?
    ): BlockStateSupplier {

        data class VariantConfig(
            val direction: Direction,
            val half: BlockHalf,
            val shape: StairShape,
            val snowy: Boolean,
            val modelId: Identifier?,
            val yRot: VariantSettings.Rotation? = null,
            val xRot: VariantSettings.Rotation? = null
        )

        fun createVariant(config: VariantConfig): BlockStateVariant =
            BlockStateVariant.create().apply {
                put(VariantSettings.MODEL, config.modelId)
                config.yRot?.let { put(VariantSettings.Y, it) }
                config.xRot?.let { put(VariantSettings.X, it) }
                put(VariantSettings.UVLOCK, true)
            }

        fun getYRotation(direction: Direction, shape: StairShape, isTop: Boolean): VariantSettings.Rotation? =
            if (isTop) {
                when (shape) {
                    StairShape.STRAIGHT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                    StairShape.OUTER_RIGHT -> when (direction) {
                        Direction.EAST -> VariantSettings.Rotation.R90
                        Direction.WEST -> VariantSettings.Rotation.R270
                        Direction.SOUTH -> VariantSettings.Rotation.R180
                        else -> null
                    }
                    StairShape.OUTER_LEFT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                    StairShape.INNER_RIGHT -> when (direction) {
                        Direction.EAST -> VariantSettings.Rotation.R90
                        Direction.WEST -> VariantSettings.Rotation.R270
                        Direction.SOUTH -> VariantSettings.Rotation.R180
                        else -> null
                    }
                    StairShape.INNER_LEFT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                }
            } else {
                when (shape) {
                    StairShape.STRAIGHT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                    StairShape.OUTER_LEFT -> when (direction) {
                        Direction.EAST -> VariantSettings.Rotation.R270
                        Direction.WEST -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R180
                        else -> null
                    }
                    StairShape.OUTER_RIGHT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                    StairShape.INNER_LEFT -> when (direction) {
                        Direction.EAST -> VariantSettings.Rotation.R270
                        Direction.WEST -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R180
                        else -> null
                    }
                    StairShape.INNER_RIGHT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                }
            }

        fun generateVariants(): List<VariantConfig> {
            val variants = mutableListOf<VariantConfig>()

            fun getModel(shape: StairShape, isTop: Boolean, snowy: Boolean): Identifier? =
                when {
                    isTop -> when (shape) {
                        StairShape.STRAIGHT -> if (snowy) snowyDefaultModelRotated else defaultModelRotated
                        StairShape.INNER_LEFT, StairShape.INNER_RIGHT -> if (snowy) snowyInnerModelRotated else innerModelRotated
                        StairShape.OUTER_LEFT, StairShape.OUTER_RIGHT -> if (snowy) snowyOuterModelRotated else outerModelRotated
                    }
                    else -> when (shape) {
                        StairShape.STRAIGHT -> if (snowy) snowyDefaultModel else regularModelId
                        StairShape.INNER_LEFT, StairShape.INNER_RIGHT -> if (snowy) snowyInnerModel else innerModelId
                        StairShape.OUTER_LEFT, StairShape.OUTER_RIGHT -> if (snowy) snowyOuterModel else outerModelId
                    }
                }

            listOf(false, true).forEach { snowy ->
                BlockHalf.entries.forEach { half ->
                    Direction.entries.filter { it.axis.isHorizontal }.forEach { direction ->
                        StairShape.entries.forEach { shape ->
                            val isTop = half == BlockHalf.TOP
                            val model = getModel(shape, isTop, snowy)

                            val yRot = getYRotation(direction, shape, isTop)
                            val xRot = if (isTop) VariantSettings.Rotation.R180 else null

                            variants.add(VariantConfig(direction, half, shape, snowy, model, yRot, xRot))
                        }
                    }
                }
            }

            return variants
        }

        return VariantsBlockStateSupplier.create(stairsBlock)
            .coordinate(BlockStateVariantMap.create(
                Properties.HORIZONTAL_FACING,
                Properties.BLOCK_HALF,
                Properties.STAIR_SHAPE,
                Properties.SNOWY
            ).apply {
                generateVariants().forEach { config ->
                    register(
                        config.direction,
                        config.half,
                        config.shape,
                        config.snowy,
                        createVariant(config)
                    )
                }
            })
    }
    /**
     * Creates a BlockStateSupplier for overgrown stairs blocks with all possible variants.
     *
     * This function generates all possible blockstate variants for a stairs block that can be overgrown.
     * It handles different stair shapes (straight, inner, outer), positions (top/bottom), and directions (N/S/E/W).
     *
     * @param stairsBlock The stairs block to create variants for
     * @param innerModelId Model for inner corner variant
     * @param regularModelId Model for straight variant
     * @param outerModelId Model for outer corner variant
     * @param innerModelRotated Model for rotated inner corner variant (top half)
     * @param defaultModelRotated Model for rotated straight variant (top half)
     * @param outerModelRotated Model for rotated outer corner variant (top half)
     * @return BlockStateSupplier containing all possible variants
     */
    fun createOvergrownStairsBlockState(
        stairsBlock: Block,
        innerModelId: Identifier,
        regularModelId: Identifier,
        outerModelId: Identifier,
        innerModelRotated: Identifier?,
        defaultModelRotated: Identifier?,
        outerModelRotated: Identifier?
    ): BlockStateSupplier {

        data class VariantConfig(
            val direction: Direction,
            val half: BlockHalf,
            val shape: StairShape,
            val modelId: Identifier?,
            val yRot: VariantSettings.Rotation? = null,
            val xRot: VariantSettings.Rotation? = null
        )

        fun createVariant(config: VariantConfig): BlockStateVariant =
            BlockStateVariant.create().apply {
                put(VariantSettings.MODEL, config.modelId)
                config.yRot?.let { put(VariantSettings.Y, it) }
                config.xRot?.let { put(VariantSettings.X, it) }
                put(VariantSettings.UVLOCK, true)
            }

        fun getYRotation(direction: Direction, shape: StairShape, isTop: Boolean): VariantSettings.Rotation? =
            if (isTop) {
                when (shape) {
                    StairShape.STRAIGHT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                    StairShape.OUTER_RIGHT -> when (direction) {
                        Direction.EAST -> VariantSettings.Rotation.R90
                        Direction.WEST -> VariantSettings.Rotation.R270
                        Direction.SOUTH -> VariantSettings.Rotation.R180
                        else -> null
                    }
                    StairShape.OUTER_LEFT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                    StairShape.INNER_RIGHT -> when (direction) {
                        Direction.EAST -> VariantSettings.Rotation.R90
                        Direction.WEST -> VariantSettings.Rotation.R270
                        Direction.SOUTH -> VariantSettings.Rotation.R180
                        else -> null
                    }
                    StairShape.INNER_LEFT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                }
            } else {
                when (shape) {
                    StairShape.STRAIGHT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                    StairShape.OUTER_LEFT -> when (direction) {
                        Direction.EAST -> VariantSettings.Rotation.R270
                        Direction.WEST -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R180
                        else -> null
                    }
                    StairShape.OUTER_RIGHT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                    StairShape.INNER_LEFT -> when (direction) {
                        Direction.EAST -> VariantSettings.Rotation.R270
                        Direction.WEST -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R180
                        else -> null
                    }
                    StairShape.INNER_RIGHT -> when (direction) {
                        Direction.WEST -> VariantSettings.Rotation.R180
                        Direction.SOUTH -> VariantSettings.Rotation.R90
                        Direction.NORTH -> VariantSettings.Rotation.R270
                        else -> null
                    }
                }
            }

        fun generateVariants(): List<VariantConfig> {
            val variants = mutableListOf<VariantConfig>()

            fun getModel(shape: StairShape, isTop: Boolean): Identifier? =
                when {
                    isTop -> when (shape) {
                        StairShape.STRAIGHT -> defaultModelRotated
                        StairShape.INNER_LEFT, StairShape.INNER_RIGHT -> innerModelRotated
                        StairShape.OUTER_LEFT, StairShape.OUTER_RIGHT -> outerModelRotated
                    }
                    else -> when (shape) {
                        StairShape.STRAIGHT -> regularModelId
                        StairShape.INNER_LEFT, StairShape.INNER_RIGHT -> innerModelId
                        StairShape.OUTER_LEFT, StairShape.OUTER_RIGHT -> outerModelId
                    }
                }

            BlockHalf.entries.forEach { half ->
                Direction.entries.filter { it.axis.isHorizontal }.forEach { direction ->
                    StairShape.entries.forEach { shape ->
                        val isTop = half == BlockHalf.TOP
                        val model = getModel(shape, isTop)

                        val yRot = getYRotation(direction, shape, isTop)
                        val xRot = if (isTop) VariantSettings.Rotation.R180 else null

                        variants.add(VariantConfig(direction, half, shape, model, yRot, xRot))
                    }
                }
            }

            return variants
        }

        return VariantsBlockStateSupplier.create(stairsBlock)
            .coordinate(BlockStateVariantMap.create(
                Properties.HORIZONTAL_FACING,
                Properties.BLOCK_HALF,
                Properties.STAIR_SHAPE
            ).apply {
                generateVariants().forEach { config ->
                    register(
                        config.direction,
                        config.half,
                        config.shape,
                        createVariant(config)
                    )
                }
            })
    }
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
     * @param snowyBottomModel Model for snowy bottom variant
     * @param snowyTopModel Model for snowy top variant
     * @param snowyFullModel Model for snowy full block variant
     * @return BlockStateSupplier containing all possible variants
     */
    fun createSnowySlabBlockState(
        slabBlock: Block,
        bottomModel: Identifier,
        topModel: Identifier,
        fullModel: Identifier?,
        snowyBottomModel: Identifier?,
        snowyTopModel: Identifier?,
        snowyFullModel: Identifier?
    ): BlockStateSupplier {
        return VariantsBlockStateSupplier.create(slabBlock)
            .coordinate(
                BlockStateVariantMap.create(Properties.SLAB_TYPE, Properties.SNOWY)
                    //non snowy
                    .register(SlabType.BOTTOM, false, BlockStateVariant.create().put(VariantSettings.MODEL, bottomModel))
                    .register(SlabType.TOP, false, BlockStateVariant.create().put(VariantSettings.MODEL, topModel))
                    .register(SlabType.DOUBLE, false, BlockStateVariant.create().put(VariantSettings.MODEL, fullModel))
                    //snowy
                    .register(SlabType.BOTTOM, true, BlockStateVariant.create().put(VariantSettings.MODEL, snowyBottomModel))
                    .register(SlabType.TOP, true, BlockStateVariant.create().put(VariantSettings.MODEL, snowyTopModel))
                    .register(SlabType.DOUBLE, true, BlockStateVariant.create().put(VariantSettings.MODEL, snowyFullModel))
            )
    }
}