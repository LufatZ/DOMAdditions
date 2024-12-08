package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.BlockStateModelGenerator.createBooleanModelMap
import net.minecraft.client.data.ItemModelGenerator
import net.minecraft.client.data.Model
import net.minecraft.client.data.Models
import net.minecraft.client.data.TextureKey
import net.minecraft.client.data.TextureMap
import net.minecraft.client.data.VariantsBlockStateSupplier
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
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
        Blocks.QUARTZ_BLOCK, Blocks.BASALT, Blocks.SMOOTH_SANDSTONE
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
     * Blocks to be excluded from standard texture generation
     */
    private val removeBlock = listOf<Block>(Blocks.MAGMA_BLOCK)

    /**
     * Blocks where bottom texture should match top texture
     */
    private val bottomAllSide = listOf<Block>(Blocks.SMOOTH_QUARTZ)

    companion object {
        /**
         * Special blocks that require completely custom model generation
         *
         * These blocks are skipped by the standard generation process
         * and would need manual model generation logic.
         */
        private val CUSTOM_MODEL_BLOCKS = setOf(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT_PATH
        )
    }

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

    /**
     * Generates item models for all registered block variants.
     *
     * Creates simple item models that reference their corresponding block models:
     * - Slabs
     * - Stairs
     * - Trapdoors
     * - Lanterns
     *
     * @param generator The ItemModelGenerator used for creating item models
     */
    override fun generateItemModels(generator: ItemModelGenerator?) {
        BlockRegistry.registeredSlabs.forEach { slab ->
            createItemModel(slab, generator)
        }
        BlockRegistry.registeredStairs.forEach { stair ->
            createItemModel(stair, generator)
        }
        BlockRegistry.registeredTrapdoors.forEach { trapdoor ->
            createItemModel(trapdoor, generator)
        }
        BlockRegistry.registeredLanterns.forEach { lantern ->
            createItemModel(lantern, generator)
        }
    }

    /**
     * Creates an individual item model for a given block.
     *
     * Generates an item model that references the block's model with appropriate parent path.
     * Handles special cases like trapdoors that need a specific model suffix.
     *
     * @param block The block for which to create an item model
     * @param generator The ItemModelGenerator used for model creation
     */
    private fun createItemModel(block: Block, generator: ItemModelGenerator?) {
        generator?.let {
            // Build the parent path for the item model
            val parentPath = buildParentPath(block)

            // Create a Model with the parent path
            val model = Model(
                Optional.of(Identifier.of(parentPath)),
                Optional.empty()
            )

            // Upload the model and register it
            it.register(block.asItem(), model)
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
                in CUSTOM_MODEL_BLOCKS -> return@forEachIndexed
                Blocks.PODZOL -> generateStairModel(this, stair, parentBlock, bottom = Blocks.DIRT)
                Blocks.MYCELIUM -> generateStairModel(this, stair, parentBlock, bottom = Blocks.DIRT)
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
                in CUSTOM_MODEL_BLOCKS -> return@forEachIndexed
                Blocks.PODZOL -> generateSlabModel(this, slab, parentBlock, bottom = Blocks.DIRT)
                Blocks.MYCELIUM -> generateSlabModel(this, slab, parentBlock, bottom = Blocks.DIRT)
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
                in CUSTOM_MODEL_BLOCKS -> return@forEachIndexed
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
                in CUSTOM_MODEL_BLOCKS -> return@forEachIndexed
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
    private fun getId(block: Block, removeBlock: Boolean = false): String =
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
    private fun getIdentifier(
        block: Block,
        side: Boolean = false,
        top: Boolean = false,
        removeBlock: Boolean = false,
        bottom: Boolean = false
    ): Identifier = buildString {
        append("block/")
        append(getId(block, removeBlock))
        if (top) append("_top")
        if (side) append("_side")
        if (bottom) append("_bottom")
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
    private fun createTextureMap(
        parent: Block,
        top: Block = parent,
        side: Block = parent,
        bottom: Block = parent,
        hasSideAndTop: Boolean = false,
        removeBlock: Boolean = false,
        bottomSameAsTop: Boolean = false,
        textureKey: TextureKey = TextureKey.ALL
    ): TextureMap = TextureMap().apply {
        val topIdentifier = getIdentifier(
            block = top,
            top = hasSideAndTop,
            removeBlock = removeBlock,
            bottom = parent in bottomAllSide
        )

        val sideIdentifier = getIdentifier(
            block = side,
            side = hasSideAndTop,
            removeBlock = removeBlock
        )

        val bottomIdentifier = getIdentifier(
            block = bottom,
            top = bottomSameAsTop,
            removeBlock = removeBlock
        )

        when (textureKey){
            TextureKey.TEXTURE -> {
                put(TextureKey.TEXTURE, topIdentifier)
            }
            else -> {
                put(TextureKey.TOP, topIdentifier)
                put(TextureKey.SIDE, sideIdentifier)
                put(TextureKey.BOTTOM, bottomIdentifier)
            }
        }
    }

    /**
     * Builds the parent path for item models.
     *
     * Handles special cases like adding a "_bottom" suffix for trapdoors.
     *
     * @param block The block for which to build the parent path
     * @return Full parent path for the item model
     */
    private fun buildParentPath(block: Block): String {
        val (namespace, path) = block.defaultState.registryEntry.idAsString.split(":")
        val suffix = if (block in BlockRegistry.registeredTrapdoors) "_bottom" else ""
        return "$namespace:block/${path}$suffix"
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
        val textureMap = createTextureMap(
            parent = parent,
            top = top,
            side = side,
            bottom = bottom,
            hasSideAndTop = parent in hasSideAndTop,
            removeBlock = parent in removeBlock,
            bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT)
        )

        val innerModel = Models.STAIRS.upload(
            stair,
            "",
            textureMap,
            generator?.modelCollector
        )

        val outerModel = Models.OUTER_STAIRS.upload(
            stair,
            "",
            textureMap,
            generator?.modelCollector
        )

        val straightModel = Models.INNER_STAIRS.upload(
            stair,
            "",
            textureMap,
            generator?.modelCollector
        )

        generator?.blockStateCollector?.accept(
            BlockStateModelGenerator.createStairsBlockState(
                stair,
                straightModel,
                innerModel,
                outerModel
            )
        )
    }

    /**
     * Generates all necessary models and blockstates for a stair variant.
     */
    private fun generateTrapdoorModel(
        generator: BlockStateModelGenerator?,
        trapdoor: Block,
        parent: Block
    ) {
        val textureMap = createTextureMap(
            parent = parent,
            hasSideAndTop = parent in hasSideAndTop,
            removeBlock = parent in removeBlock,
            textureKey = TextureKey.TEXTURE,
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
            put(TextureKey.TEXTURE, Identifier.of("block/${getId(parent)}"))
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
        val textureMap = createTextureMap(
            parent = parent,
            top = top,
            side = side,
            bottom = bottom,
            hasSideAndTop = parent in hasSideAndTop,
            removeBlock = parent in removeBlock,
            bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT)
        )

        val bottomModel = Models.SLAB.upload(
            slab,
            "",
            textureMap,
            generator?.modelCollector
        )

        val topModel = Models.SLAB_TOP.upload(
            slab,
            "",
            textureMap,
            generator?.modelCollector
        )

        val fullBlockId = Identifier.of("block/${getId(parent)}")

        generator?.blockStateCollector?.accept(
            BlockStateModelGenerator.createSlabBlockState(
                slab,
                bottomModel,
                topModel,
                fullBlockId
            )
        )
    }
}