package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.GrassBlock
import net.minecraft.block.SnowBlock
import net.minecraft.block.enums.BlockHalf
import net.minecraft.block.enums.SlabType
import net.minecraft.block.enums.StairShape
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.BlockStateModelGenerator.createBooleanModelMap
import net.minecraft.client.data.BlockStateModelGenerator.createStairsBlockState
import net.minecraft.client.data.BlockStateSupplier
import net.minecraft.client.data.BlockStateVariant
import net.minecraft.client.data.BlockStateVariantMap
import net.minecraft.client.data.ItemModelGenerator
import net.minecraft.client.data.ItemModels
import net.minecraft.client.data.Model
import net.minecraft.client.data.Models
import net.minecraft.client.data.TextureKey
import net.minecraft.client.data.TextureMap
import net.minecraft.client.data.VariantSettings
import net.minecraft.client.data.VariantsBlockStateSupplier
import net.minecraft.client.render.item.tint.TintSource
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import net.minecraft.util.math.Direction
import java.util.Optional

/**
 * Comprehensive Model Generation Class
 *
 * Handles the intricate process of generating models and textures for various block types.
 * Supports complex scenarios like different textures for block sides, special block variants,
 * and custom texture mappings.
 *
 * Key Features:
 * - Dynamic model generation for stairs, slabs, trapdoors, and lanterns
 * - Intelligent texture mapping based on parent block characteristics
 * - Flexible handling of block-specific model generation rules
 *
 * @param generator The FabricDataOutput used for generating mod resources
 */
class ModelGenerator(generator: FabricDataOutput) : FabricModelProvider(generator) {
    /**
     * Configuration Lists for Special Block Texture Handling
     *
     * These lists and maps define special rules for texture generation for specific block types.
     * They help manage unique cases where block textures differ from standard generation methods.
     */
    private val hasSideAndTop = listOf<Block>(
        Blocks.PODZOL, Blocks.MYCELIUM, Blocks.POLISHED_BASALT,
        Blocks.MUDDY_MANGROVE_ROOTS, Blocks.SMOOTH_RED_SANDSTONE,
        Blocks.QUARTZ_BLOCK, Blocks.BASALT, Blocks.SMOOTH_SANDSTONE,
        Blocks.DIRT_PATH, Blocks.GRASS_BLOCK
    )

    /**
     * Mapping for blocks with alternative texture sources
     *
     * Used when a block's texture should be derived from another block,
     * typically for smoothed or processed variants.
     */
    private val hasNoTexture = mapOf<Block, Block>(
        Blocks.SMOOTH_RED_SANDSTONE to Blocks.RED_SANDSTONE,
        Blocks.SMOOTH_QUARTZ to Blocks.QUARTZ_BLOCK,
        Blocks.SMOOTH_SANDSTONE to Blocks.SANDSTONE
    )

    /**
     * Blocks where "_block" should be removed
     */
    private val removeBlock = listOf<Block>(Blocks.MAGMA_BLOCK)

    /**
     * Blocks where bottom texture should match top texture
     */
    private val bottomAllSide = listOf<Block>(Blocks.SMOOTH_QUARTZ)

    /**
     * Generates block state models for all registered block variants.
     *
     * Calls specific generation methods for different block types:
     * - Stairs
     * - Slabs
     * - Trapdoors
     * - Lanterns
     *
     * @param generator The BlockStateModelGenerator used for creating block state models
     */
    override fun generateBlockStateModels(generator: BlockStateModelGenerator?) {
        with(generator) {
            // Generate models for registered blocks
            generateStairModels()
            generateSlabModels()
            generateTrapdoorModels()
            generateLanternModels()
        }
    }

    override fun generateItemModels(itemModelGenerator: ItemModelGenerator?) {
        //actually not needed
    }

    /**
     * Generates an item model for the given block.
     * Depending on the type of the parent block, the item model may be tinted.
     *
     * @param block The block for which the item model is to be generated.
     * @param parentBlock The parent block (e.g., if `block` is a DirtSlab, the `parentBlock` is Dirt).
     *                    Determines whether the model should be tinted.
     * @param model The identifier of the model to associate with the item.
     * @param generator The BlockStateModelGenerator responsible for registering block state models.
     *                  If null, no model will be registered.
     */
    private fun generateBlockItemModel(
        block: Block,
        parentBlock: Block,
        model: Identifier,
        generator: BlockStateModelGenerator?
    ) {
        val grassColor: TintSource = ItemModels.constantTintSource(0x91BD59)
        when (parentBlock) {
            is GrassBlock -> generator?.registerTintedItemModel(block, model, grassColor)
            else -> generator?.registerParentedItemModel(block, model)
        }
    }

    /**
     * Processes and generates models for all registered stairs.
     *
     * Handles special cases for certain parent blocks like Podzol and Mycelium,
     * which require custom bottom texture handling.
     */
    private fun BlockStateModelGenerator?.generateStairModels() {
        BlockRegistry.registeredStairs.forEachIndexed { index, stair ->
            val parentBlock = BlockRegistry.blockVariantsParents[index]

            when (parentBlock) {
                Blocks.PODZOL -> generateStairModel(this, stair, parentBlock, bottom = Blocks.DIRT)
                Blocks.MYCELIUM -> generateStairModel(this, stair, parentBlock, bottom = Blocks.DIRT)
                Blocks.DIRT_PATH -> generateStairModel(this, stair, parentBlock, bottom = Blocks.DIRT)
                Blocks.GRASS_BLOCK -> generateStairModel(this, stair, parentBlock, bottom = Blocks.DIRT)
                else -> generateStairModel(this, stair, parentBlock)
            }
        }
    }

    /**
     * Processes and generates models for all registered slabs.
     *
     * Similar to stair generation, handles special cases for parent blocks
     * that require custom texture mapping.
     */
    private fun BlockStateModelGenerator?.generateSlabModels() {
        BlockRegistry.registeredSlabs.forEachIndexed { index, slab ->
            val parentBlock = BlockRegistry.blockVariantsParents[index]

            when (parentBlock) {
                Blocks.PODZOL -> generateSlabModel(this, slab, parentBlock, bottom = Blocks.DIRT)
                Blocks.MYCELIUM -> generateSlabModel(this, slab, parentBlock, bottom = Blocks.DIRT)
                Blocks.DIRT_PATH -> generateSlabModel(this, slab, parentBlock, bottom = Blocks.DIRT)
                Blocks.GRASS_BLOCK -> generateSlabModel(this, slab, parentBlock, bottom = Blocks.DIRT)
                else -> generateSlabModel(this, slab, parentBlock)
            }
        }
    }

    /**
     * Processes and generates models for all registered lanterns.
     *
     * Creates standing and hanging variants for each registered lantern,
     * using the parent block's texture as a base.
     */
    private fun BlockStateModelGenerator?.generateLanternModels() {
        BlockRegistry.registeredLanterns.forEachIndexed { index, lantern ->
            val parentBlock = BlockRegistry.lanternVariantsParents[index]

            when (parentBlock) {
                else -> generateLanternModel(this, lantern, parentBlock)
            }
        }
    }

    /**
     * Processes and generates models for all registered trapdoors.
     *
     * Creates bottom, top, and open variants for each registered trapdoor,
     * using the parent block's texture.
     */
    private fun BlockStateModelGenerator?.generateTrapdoorModels() {
        BlockRegistry.registeredTrapdoors.forEachIndexed { index, trapdoor ->
            val parentBlock = BlockRegistry.trapdoorVariantsParents[index]

            when (parentBlock) {
                else -> generateTrapdoorModel(this, trapdoor, parentBlock)
            }
        }
    }

    /**
     * Extracts a clean block ID from a block's registry entry.
     *
     * Removes the namespace and optionally removes the "_block" suffix.
     *
     * @param block The block to extract ID from
     * @param removeBlock Whether to remove the "_block" suffix
     * @return Cleaned block identifier
     */
    private fun extractCleanBlockIdentifier(block: Block, removeBlock: Boolean = false): String =
        block.defaultState.registryEntry.idAsString
            .replace("minecraft:", "")
            .let { if (removeBlock) it.replace("_block", "") else it }

    /**
     * Builds a full texture identifier for a block.
     *
     * Supports generating identifiers with various suffixes like "_side", "_top", etc.
     *
     * @param block The source block
     * @param side Whether to append "_side" suffix
     * @param top Whether to append "_top" suffix
     * @param removeBlock Whether to remove "_block" from the identifier
     * @param bottom Whether to append "_bottom" suffix
     * @return Full texture identifier
     */
    private fun buildTextureIdentifier(
        block: Block,
        side: Boolean = false,
        top: Boolean = false,
        removeBlock: Boolean = false,
        bottom: Boolean = false,
        overlay: Boolean = false,
        snow: Boolean = false
    ): Identifier = buildString {
        append("block/")
        append(extractCleanBlockIdentifier(block, removeBlock))
        if (top) append("_top")
        if (side) append("_side")
        if (bottom) append("_bottom")
        if (overlay) append("_overlay")
        if (snow) append("_snow")
    }.let { Identifier.of(it) }

    /**
     * Creates a TextureMap for a block with flexible texture configuration.
     *
     * Supports complex texture mapping scenarios like:
     * - Different textures for top, side, and bottom
     * - Handling blocks with special texture rules
     *
     * @param parent The primary block for texture reference
     * @param top Block used for top texture (defaults to parent)
     * @param side Block used for side texture (defaults to parent)
     * @param bottom Block used for bottom texture (defaults to parent)
     * @param hasSideAndTop Whether the block has distinct side and top textures
     * @param removeBlock Whether to remove "_block" from texture paths
     * @param bottomSameAsTop Whether bottom texture should match top texture
     * @param textureKey Specific texture key to use (defaults to ALL)
     * @return Configured TextureMap for model generation
     */
    private fun configureBlockTextureMapping(
        parent: Block,
        top: Block = parent,
        side: Block = parent,
        bottom: Block = parent,
        hasSideAndTop: Boolean = false,
        removeBlock: Boolean = false,
        bottomSameAsTop: Boolean = false,
        textureKey: String = "default"
    ): TextureMap = TextureMap().apply {
        val topIdentifier = buildTextureIdentifier(
            block = top,
            top = hasSideAndTop && top !is SnowBlock,
            removeBlock = removeBlock,
            bottom = parent in bottomAllSide
        )

        val sideIdentifier = buildTextureIdentifier(
            block = side,
            side = hasSideAndTop && top !is SnowBlock,
            removeBlock = removeBlock,
            snow = top is SnowBlock && parent !is SnowBlock
        )

        val bottomIdentifier = buildTextureIdentifier(
            block = bottom,
            top = bottomSameAsTop,
            removeBlock = removeBlock
        )

        val overlayIdentifier = buildTextureIdentifier(
            block = parent,
            side = hasSideAndTop,
            overlay = true
        )

        when (textureKey) {
            "texture" -> {
                put(TextureKey.TEXTURE, topIdentifier)
            }

            "overlay" -> {
                put(TextureKey.TOP, topIdentifier)
                put(TextureKey.SIDE, sideIdentifier)
                put(TextureKey.BOTTOM, bottomIdentifier)
                put(TextureKey.LAYER0, overlayIdentifier)
            }

            else -> {
                put(TextureKey.TOP, topIdentifier)
                put(TextureKey.SIDE, sideIdentifier)
                put(TextureKey.BOTTOM, bottomIdentifier)
            }
        }
    }

    /**
     * Generates all necessary models and blockstates for a stair variant.
     */
    private fun generateStairModel(
        generator: BlockStateModelGenerator?,
        stair: Block,
        parent: Block,
        top: Block = parent,
        side: Block = parent,
        bottom: Block = parent
    ) {
        val textureMap = configureBlockTextureMapping(
            parent = parent,
            top = top,
            side = side,
            bottom = bottom,
            hasSideAndTop = parent in hasSideAndTop,
            removeBlock = parent in removeBlock,
            bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
            textureKey = if (parent is GrassBlock) "overlay" else ""
        )

        val snowyTextureMap = configureBlockTextureMapping(
            parent = parent,
            top = Blocks.SNOW,
            side = side,
            bottom = bottom,
            hasSideAndTop = parent in hasSideAndTop,
            removeBlock = parent in removeBlock,
            bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
            textureKey = ""
        )

        // Regular models
        val defaultModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM,
                TextureKey.LAYER0
            ).upload(stair, "", textureMap, generator?.modelCollector)

            else -> Models.STAIRS.upload(stair, "", textureMap, generator?.modelCollector)
        }

        val defaultModelRotated = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_rotated")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM,
                TextureKey.LAYER0
            ).upload(stair, "_rotated", textureMap, generator?.modelCollector)

            else -> Models.STAIRS.upload(stair, "_rotated", textureMap, generator?.modelCollector)
        }

        val outerModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_outer")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM,
                TextureKey.LAYER0
            ).upload(stair, "_outer", textureMap, generator?.modelCollector)

            else -> Models.OUTER_STAIRS.upload(stair, "", textureMap, generator?.modelCollector)
        }

        val outerModelRotated = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_outer_rotated")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM,
                TextureKey.LAYER0
            ).upload(stair, "_outer_rotated", textureMap, generator?.modelCollector)

            else -> Models.OUTER_STAIRS.upload(stair, "_rotated", textureMap, generator?.modelCollector)
        }

        val innerModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_inner")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM,
                TextureKey.LAYER0
            ).upload(stair, "_inner", textureMap, generator?.modelCollector)

            else -> Models.INNER_STAIRS.upload(stair, "", textureMap, generator?.modelCollector)
        }

        val innerModelRotated = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_inner_rotated")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM,
                TextureKey.LAYER0
            ).upload(stair, "_inner_rotated", textureMap, generator?.modelCollector)

            else -> Models.INNER_STAIRS.upload(stair, "_rotated", textureMap, generator?.modelCollector)
        }

        // Snowy models for grass blocks
        val snowyDefaultModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_snowy")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM
            ).upload(stair, "_snowy", snowyTextureMap, generator?.modelCollector)

            else -> null
        }

        val snowyDefaultModelRotated = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_snowy_rotated")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM
            ).upload(stair, "_snowy_rotated", snowyTextureMap, generator?.modelCollector)

            else -> null
        }

        val snowyOuterModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_outer_snowy")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM
            ).upload(stair, "_snowy_outer", snowyTextureMap, generator?.modelCollector)

            else -> null
        }

        val snowyOuterModelRotated = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_outer_snowy_rotated")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM
            ).upload(stair, "_snowy_outer_rotated", snowyTextureMap, generator?.modelCollector)

            else -> null
        }

        val snowyInnerModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_inner_snowy")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM
            ).upload(stair, "_snowy_inner", snowyTextureMap, generator?.modelCollector)

            else -> null
        }

        val snowyInnerModelRotated = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_stair_inner_snowy_rotated")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM
            ).upload(stair, "_snowy_inner_rotated", snowyTextureMap, generator?.modelCollector)

            else -> null
        }

        generateBlockItemModel(stair, parent, defaultModel, generator)

        generator?.blockStateCollector?.accept(
            when (parent) {
                !is GrassBlock -> createStairsBlockState(
                    stair,
                    innerModel,
                    defaultModel,
                    outerModel
                )
                else -> createSnowyStairsBlockState(
                    stair,
                    innerModel,
                    defaultModel,
                    outerModel,
                    innerModelRotated,
                    defaultModelRotated,
                    outerModelRotated,
                    snowyInnerModel,
                    snowyDefaultModel,
                    snowyOuterModel,
                    snowyInnerModelRotated,
                    snowyDefaultModelRotated,
                    snowyOuterModelRotated
                )
            }
        )
    }

    /**
     * Generates all necessary models and blockstates for a trapdoor variant.
     */
    private fun generateTrapdoorModel(
        generator: BlockStateModelGenerator?,
        trapdoor: Block,
        parent: Block
    ) {
        val textureMap = configureBlockTextureMapping(
            parent = parent,
            hasSideAndTop = parent in hasSideAndTop,
            removeBlock = parent in removeBlock,
            textureKey = "texture",
            top = (if (parent in hasNoTexture) hasNoTexture[parent] else parent)!!
        )

        val bottomModel = Models.TEMPLATE_TRAPDOOR_BOTTOM.upload(
            trapdoor,
            "",
            textureMap,
            generator?.modelCollector
        )

        val topModel = Models.TEMPLATE_TRAPDOOR_TOP.upload(
            trapdoor,
            "",
            textureMap,
            generator?.modelCollector
        )

        val openModel = Models.TEMPLATE_TRAPDOOR_OPEN.upload(
            trapdoor,
            "",
            textureMap,
            generator?.modelCollector
        )

        generateBlockItemModel(trapdoor, parent, bottomModel, generator)

        generator?.blockStateCollector?.accept(
            BlockStateModelGenerator.createTrapdoorBlockState(
                trapdoor,
                topModel,
                bottomModel,
                openModel
            )
        )
    }

    /**
     * Generates all necessary models and blockstates for a lantern variant.
     */
    private fun generateLanternModel(
        generator: BlockStateModelGenerator?,
        lantern: Block,
        parent: Block
    ) {
        val textureMap = TextureMap().apply {
            put(TextureKey.TEXTURE, Identifier.of("block/${extractCleanBlockIdentifier(parent)}"))
            put(TextureKey.PARTICLE, Identifier.of("block/lantern"))
        }

        val lanternModelStanding = Model(
            Optional.of(Identifier.of("${MODID}:block/template_lantern_standing")),
            Optional.empty(),
            TextureKey.TEXTURE,
            TextureKey.PARTICLE
        ).upload(
            lantern,
            "",
            textureMap,
            generator?.modelCollector
        )
        val lanternModelhanging = Model(
            Optional.of(Identifier.of("${MODID}:block/template_lantern_hanging")),
            Optional.empty(),
            TextureKey.TEXTURE,
            TextureKey.PARTICLE
        ).upload(
            lantern,
            "_hanging",
            textureMap,
            generator?.modelCollector
        )

        generateBlockItemModel(lantern, parent, lanternModelStanding, generator)

        generator?.blockStateCollector?.accept(
            VariantsBlockStateSupplier.create(lantern)
                .coordinate(createBooleanModelMap(Properties.HANGING, lanternModelhanging, lanternModelStanding))
        )
    }


    /**
     * Generates all necessary models and blockstates for a slab variant.
     */
    private fun generateSlabModel(
        generator: BlockStateModelGenerator?,
        slab: Block,
        parent: Block,
        top: Block = parent,
        side: Block = parent,
        bottom: Block = parent
    ) {
        val textureMap = configureBlockTextureMapping(
            parent = parent,
            top = top,
            side = side,
            bottom = bottom,
            hasSideAndTop = parent in hasSideAndTop,
            removeBlock = parent in removeBlock,
            bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
            textureKey = if (parent is GrassBlock) {
                "overlay"
            } else ""
        )

        val snowyTextureMap = configureBlockTextureMapping(
            parent = parent,
            top = Blocks.SNOW,
            side = side,
            bottom = bottom,
            hasSideAndTop = parent in hasSideAndTop,
            removeBlock = parent in removeBlock,
            bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
            textureKey = ""
        )

        val bottomModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_slab_bottom")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM,
                TextureKey.LAYER0
            ).upload(
                slab,
                "",
                textureMap,
                generator?.modelCollector
            )

            else -> Models.SLAB.upload(
                slab,
                "",
                textureMap,
                generator?.modelCollector
            )
        }

        val snowyBottomModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_snowy_grass_slab_bottom")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM
            ).upload(
                slab,
                "_snow",
                snowyTextureMap,
                generator?.modelCollector
            )

            else -> null
        }

        val topModel = when (parent) {
            is GrassBlock -> Model(
                Optional.of(Identifier.of("${MODID}:block/template_grass_slab_top")),
                Optional.empty(),
                TextureKey.TOP,
                TextureKey.SIDE,
                TextureKey.BOTTOM,
                TextureKey.LAYER0
            ).upload(
                slab,
                "_top",
                textureMap,
                generator?.modelCollector
            )

            else -> Models.SLAB_TOP.upload(
                slab,
                "",
                textureMap,
                generator?.modelCollector
            )
        }


        val snowyTopModel = when (parent) {
            is GrassBlock -> Models.SLAB_TOP.upload(
                slab,
                "_snow_top",
                snowyTextureMap,
                generator?.modelCollector
            )

            else -> null
        }

        val fullBlockModel = Identifier.of("block/${extractCleanBlockIdentifier(parent)}")

        val snowyFullBlockModel =when (parent){is GrassBlock ->  Identifier.of("block/${extractCleanBlockIdentifier(parent)}_snow") else -> null}

        generateBlockItemModel(slab, parent, bottomModel, generator)

        generator?.blockStateCollector?.accept(
            if (parent is GrassBlock) {
                createSnowySlabBlockState(
                    slab,
                    bottomModel,
                    topModel,
                    fullBlockModel,
                    snowyBottomModel,
                    snowyTopModel,
                    snowyFullBlockModel
                )
            }else {
                BlockStateModelGenerator.createSlabBlockState(
                    slab,
                    bottomModel,
                    topModel,
                    fullBlockModel
                )
            }
        )
    }
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