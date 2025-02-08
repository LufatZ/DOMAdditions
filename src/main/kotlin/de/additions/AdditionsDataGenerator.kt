package de.additions

import de.additions.blocks.BlockRegistry
import de.additions.datagen.AdditionsRecipeGenerator
import de.additions.datagen.BlockTagGenerator
import de.additions.datagen.ItemTagGenerator
import de.additions.datagen.LootGenerator
import de.additions.datagen.ModelGenerator
import de.additions.datagen.TranslationGenerator
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import kotlin.apply

/**
 * Comprehensive Data Generation System for the Additions Minecraft Mod
 *
 * Purpose:
 * This object serves as the primary data generator for the Additions mod,
 * responsible for automatically generating block models, item models,
 * block states, and associated textures during the mod's build process.
 *
 * Key Responsibilities:
 * - Automatically generate models for custom block variants (stairs, slabs, trapdoors, lanterns)
 * - Create appropriate item models for registered blocks
 * - Handle texture mapping for different block types
 * - Support special cases for texture generation (e.g., blocks with side and top textures)
 *
 * Design Considerations:
 * - Uses Fabric Mod's data generation API for seamless integration
 * - Provides flexible texture and model generation for various block types
 * - Supports custom block variant generation with parent block references
 *
 * @see BlockRegistry for registered block collections
 * @see FabricDataGenerator for data generation framework
 */
object AdditionsDataGenerator : DataGeneratorEntrypoint {
	/**
	 * Entry point for initializing the data generator.
	 *
	 * This method sets up the data generation process by creating a data pack
	 * and adding the ModelGenerator as a provider.
	 *
	 * @param generator The Fabric data generator responsible for creating mod resources
	 */
	override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
		generator.createPack().apply {
			// Add the custom ModelGenerator to handle model and texture generation
			addProvider(::ModelGenerator)
			// Add the custom LootGenerator to handle loot table generation
			addProvider(::LootGenerator)
			// Add the custom BlockTagGenerator to handle block tag generation (e.g. mine able by...)
			addProvider(::BlockTagGenerator)
			// Add the custom ItemTagGenerator to handle item tag generation (e.g. stone blockItems...)
			addProvider(::ItemTagGenerator)
			// Add the custom RecipeGenerator to handle recipe generation for crafting
			addProvider(::AdditionsRecipeGenerator)
			//
			addProvider(::TranslationGenerator)
		}
	}
}