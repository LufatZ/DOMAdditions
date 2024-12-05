package de.additions

import com.google.common.base.Supplier
import com.google.gson.JsonObject
import de.additions.Additions.MODID
import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.data.client.*
import net.minecraft.data.client.BlockStateModelGenerator.createBooleanModelMap
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
				generateChainModels()
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
		 * Creates individual item models for slabs by referencing their block models.
		 */
		private fun createItemModel(block: Block, generator: ItemModelGenerator?) {
				val modelPath = buildParentPath(block)
				val modelJson = createItemModelJson(modelPath)

				generator?.writer?.accept(
					ModelIds.getItemModelId(block.asItem()),
					Supplier { modelJson }
				)
		}

		/**
		 * Processes all registered chains.
		 */
		private fun BlockStateModelGenerator?.generateChainModels() {
			BlockRegistry.registeredChains.forEach { chain ->
				// TODO: Implement chain registration
				// this?.registerSimpleCubeAll(chain)
			}
		}

		/**
		 * Processes all registered stairs.
		 */
		private fun BlockStateModelGenerator?.generateStairModels() {
			BlockRegistry.registeredStairs.forEachIndexed { index, stair ->
				val parentBlock = BlockRegistry.blockVariantsParents[index]

				// Skip custom model blocks
				if (parentBlock in CUSTOM_MODEL_BLOCKS) return@forEachIndexed
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
			removeBlock: Boolean = false
		): Identifier = buildString {
			append("block/")
			append(getId(block, removeBlock))
			if (top) append("_top")
			if (side) append("_side")
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
			bottomSameAsTop: Boolean = false
		): TextureMap = TextureMap().apply {
			if (!hasSideAndTop) {
				put(TextureKey.TOP, getIdentifier(top,removeBlock=removeBlock))
				put(TextureKey.SIDE, getIdentifier(side,removeBlock=removeBlock))
			} else {
				put(TextureKey.TOP, getIdentifier(top, top = true,removeBlock=removeBlock))
				put(TextureKey.SIDE, getIdentifier(side, side = true,removeBlock=removeBlock))
			}
			if (bottomSameAsTop){
				put(TextureKey.BOTTOM, getIdentifier(bottom, top = true, removeBlock = removeBlock))
			}
			else {
				put(TextureKey.BOTTOM, getIdentifier(bottom, removeBlock = removeBlock))
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
				hasSideAndTop = parent in listOf<Block>(Blocks.PODZOL, Blocks.MYCELIUM, Blocks.POLISHED_BASALT, Blocks.MUDDY_MANGROVE_ROOTS),
				removeBlock = parent == Blocks.MAGMA_BLOCK,
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
			val newTextureMap: TextureMap = TextureMap().apply {
				put(TextureKey.TEXTURE, getIdentifier(parent))
			}

			val bottomModel = Models.TEMPLATE_TRAPDOOR_BOTTOM.upload(
				trapdoor,
				"",
				newTextureMap,
				generator?.modelCollector
			)

			val topModel = Models.TEMPLATE_TRAPDOOR_TOP.upload(
				trapdoor,
				"",
				newTextureMap,
				generator?.modelCollector
			)

			val openModel = Models.TEMPLATE_TRAPDOOR_OPEN.upload(
				trapdoor,
				"",
				newTextureMap,
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
				hasSideAndTop = parent in listOf<Block>(Blocks.PODZOL, Blocks.MYCELIUM, Blocks.POLISHED_BASALT, Blocks.MUDDY_MANGROVE_ROOTS),
				removeBlock = parent == Blocks.MAGMA_BLOCK,
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


		/**
		 * Creates a JSON object for item models.
		 */
		private fun createItemModelJson(parentPath: String): JsonObject =
			JsonObject().apply {
				addProperty("parent", parentPath)
			}
	}
}