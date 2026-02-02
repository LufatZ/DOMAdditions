package de.additions

import de.additions.datagen.*
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator

/**
 * Entry point for the Additions mod's data generation process.
 *
 * This object implements [DataGeneratorEntrypoint] to register various data providers
 * that automatically generate assets like models, block states, recipes, loot tables,
 * tags, and translations during the build process.
 *
 * Based on Fabric's internal data generation runner, all providers are registered
 * within the single `onInitializeDataGenerator` method, even if they depend
 * on client-side classes like `ModelGenerator`. Fabric Loom's data generation
 * environment is expected to handle the loading of necessary classes for these providers.
 *
 * @see FabricDataGenerator The main controller for data generation.
 * @see ModelGenerator Provider for block/item models (client-specific dependency).
 * @see LootGenerator Provider for block loot tables.
 * @see BlockTagGenerator Provider for block tags.
 * @see ItemTagGenerator Provider for item tags.
 * @see RecipeGenerator Provider for crafting recipes.
 * @see TranslationGenerator Provider for language translations.
 */
object AdditionsDataGenerator : DataGeneratorEntrypoint {
    /**
     * Registers all data providers for the mod.
     *
     * This method is called by Fabric during data generation setup. It registers
     * all necessary providers, including those with client-side dependencies like
     * the [ModelGenerator], within this single entry point based on how Fabric's
     * internal runner appears to operate.
     *
     * @param generator The Fabric data generator instance, used to create data packs and register providers.
     */
    override fun onInitializeDataGenerator(generator: FabricDataGenerator) {
        // Create a data pack associated with this generator run.
        val pack: FabricDataGenerator.Pack = generator.createPack()

        // Register all data providers here.
        pack.addProvider(::ModelGenerator) // Generates block and item models (requires client classes).
        pack.addProvider(::LootGenerator) // Generates block loot tables.
        pack.addProvider(::BlockTagGenerator) // Generates block tags (e.g., mineable tags).
        pack.addProvider(::ItemTagGenerator) // Generates item tags.
        pack.addProvider(::RecipeGenerator) // Generates crafting recipes.
        pack.addProvider(::TranslationGenerator) // Generates language files (.lang or .json).
    }

    // No onInitializeClientDataGenerator needed based on FabricDataGenHelper.java logic
}
