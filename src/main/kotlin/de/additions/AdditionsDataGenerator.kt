package de.additions

import de.additions.Additions.MODID
import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
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
import kotlin.apply

/**
 * Main DataGenerator entry point for the Additions mod.
 * Handles the generation of block models, states, and textures.
 */
object AdditionsDataGenerator : DataGeneratorEntrypoint {
	override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
		generator.createPack().apply {
			addProvider(::ModelGenerator)
		}
	}

	/**
	 * Main model generator class handling both block and item model generation.
	 */
	private class ModelGenerator(generator: FabricDataOutput) : FabricModelProvider(generator) {
		private val hasSideAndTop = listOf<Block>(Blocks.PODZOL, Blocks.MYCELIUM, Blocks.POLISHED_BASALT, Blocks.MUDDY_MANGROVE_ROOTS, Blocks.SMOOTH_RED_SANDSTONE,
			Blocks.QUARTZ_BLOCK,Blocks.BASALT,Blocks.SMOOTH_SANDSTONE)
		private val hasNoTexture = mapOf<Block, Block>(Blocks.SMOOTH_RED_SANDSTONE to Blocks.RED_SANDSTONE, Blocks.SMOOTH_QUARTZ to Blocks.QUARTZ_BLOCK,
			Blocks.SMOOTH_SANDSTONE to Blocks.SANDSTONE)
		private val removeBlock = listOf<Block>(Blocks.MAGMA_BLOCK)
		private val bottomAllSide = listOf<Block>(Blocks.SMOOTH_QUARTZ)

		companion object {
			/**
			 * List of blocks that have custom model generation logic and should be skipped
			 */
			private val CUSTOM_MODEL_BLOCKS = setOf(
				Blocks.GRASS_BLOCK,
				Blocks.DIRT_PATH
			)
		}

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
		 * Generates item models for all registered slabs.
		 * Creates a simple parent reference to the block model.
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
		 * Creates individual item models for blocks by referencing their block models.
		 * This method ensures that item models are generated with the correct parent block model.
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
		 * Processes all registered stairs.
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
		 * Processes all registered slabs with their parent blocks.
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
		 * Processes all registered lanterns.
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
		 * Processes all registered trapdoors.
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
		 * Extracts clean block ID without namespace and optionally removes "_block" suffix.
		 */
		private fun getId(block: Block, removeBlock: Boolean = false): String =
			block.defaultState.registryEntry.idAsString
				.replace("minecraft:", "")
				.let { if (removeBlock) it.replace("_block", "") else it }

		/**
		 * Builds the full identifier for a block texture.
		 * @param block The source block
		 * @param side Whether to append "_side" suffix
		 * @param top Whether to append "_top" suffix
		 * @param removeBlock Whether to remove "_block" from the identifier
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
		 * Creates a TextureMap for a block with optional different textures for top, side, and bottom.
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

		/**
		 * Creates the parent path for item models.
		 */
		private fun buildParentPath(block: Block): String {
			val (namespace, path) = block.defaultState.registryEntry.idAsString.split(":")
			val suffix = if (block in BlockRegistry.registeredTrapdoors) "_bottom" else ""
			return "$namespace:block/${path}$suffix"
		}
	}
}