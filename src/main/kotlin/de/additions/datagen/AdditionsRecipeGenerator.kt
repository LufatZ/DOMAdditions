package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.LanternBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.block.TrapdoorBlock
import net.minecraft.data.recipe.RecipeExporter
import net.minecraft.data.recipe.RecipeGenerator
import net.minecraft.data.recipe.ShapedRecipeJsonBuilder
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.item.Items
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

/**
 * AdditionsRecipeGenerator is a specialized recipe generation class for dayofmind,
 * responsible for automatically creating crafting recipes for various block variants.
 *
 * Key Responsibilities:
 * - Generate recipes for stairs, slabs, trapdoors, and custom lanterns
 * - Automatically create recipes based on base block materials
 * - Support multiple recipe variants (e.g., lanterns created from different base items)
 *
 * Design Pattern: Uses a custom RecipeGenerator with a specialized generate() method
 *
 * @property output FabricDataOutput for writing generated recipe files
 * @property registriesFuture Provides access to game registries during recipe generation
 */
class AdditionsRecipeGenerator(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricRecipeProvider(output, registriesFuture) {

    /**
     * Overrides the default recipe generation method to provide custom recipe generation logic.
     *
     * @param registryLookup Provides access to game registries
     * @param exporter Handles exporting generated recipes
     * @return A custom RecipeGenerator that calls our specialized generation method
     */
    override fun getRecipeGenerator(
        registryLookup: RegistryWrapper.WrapperLookup,
        exporter: RecipeExporter
    ): RecipeGenerator? {
        // Create an anonymous RecipeGenerator that calls our custom generation method
        return object : RecipeGenerator(registryLookup, exporter) {
            override fun generate() {
                // Invoke method to generate recipes for various block types
                generateRecipes(registryLookup, exporter)
            }
        }
    }

    /**
     * Generates recipes for different block variants stored in BlockRegistry.
     *
     * Generates recipes for:
     * - Stairs blocks (4 items per craft)
     * - Slab blocks (6 items per craft)
     * - Trapdoor blocks (1 item per craft)
     * - Lantern blocks with multiple base material options
     *
     * @param registryLookup Provides access to game registries
     * @param exporter Handles exporting generated recipes
     */
    private fun generateRecipes(
        registryLookup: RegistryWrapper.WrapperLookup,
        exporter: RecipeExporter
    ) {
        // Retrieve registered blocks from BlockRegistry
        val stairsBlocks = BlockRegistry.registeredStairs
        val baseBlocks = BlockRegistry.blockVariantsParents
        val slabsBlocks = BlockRegistry.registeredSlabs
        val trapdoorBlocks = BlockRegistry.registeredTrapdoors
        val trapdoorParents = BlockRegistry.trapdoorVariantsParents
        val lanternBlocks = BlockRegistry.registeredLanterns
        val lanternParents = BlockRegistry.lanternVariantsParents

        // Generate stairs recipes
        stairsBlocks.forEachIndexed { index, stairBlock ->
            createRecipe(
                registryLookup,
                stairBlock,
                baseBlocks[index].asItem(),
                exporter,
                4  // 4 stairs per craft
            )
        }

        // Generate slab recipes
        slabsBlocks.forEachIndexed { index, slabBlock ->
            createRecipe(
                registryLookup,
                slabBlock,
                baseBlocks[index].asItem(),
                exporter,
                6  // 6 slabs per craft
            )
        }

        // Generate trapdoor recipes
        trapdoorBlocks.forEachIndexed { index, trapdoorBlock ->
            createRecipe(
                registryLookup,
                trapdoorBlock,
                trapdoorParents[index].asItem(),
                exporter,
                1
            )
        }

        // Generate lantern recipes from different base materials
        // TODO: Refactor to use more dynamic material registration
        lanternBlocks.forEachIndexed { index, lanternBlock ->
            // Lantern recipe from candle
            createRecipe(
                registryLookup,
                lanternBlock,
                Items.CANDLE,
                exporter,
                1,
                lanternParents[index].asItem(),
                "_from_candle"
            )

            // Lantern recipe from torch
            createRecipe(
                registryLookup,
                lanternBlock,
                Items.TORCH,
                exporter,
                1,
                lanternParents[index].asItem(),
                "_from_torch"
            )

            // Lantern recipe from existing lantern
            createRecipe(
                registryLookup,
                lanternBlock,
                Blocks.LANTERN.asItem(),
                exporter,
                1,
                lanternParents[index].asItem(),
                "_from_lantern"
            )
        }
    }

    /**
     * Creates a shaped recipe for a specific block type with configurable parameters.
     *
     * Supports different crafting patterns based on block type:
     * - Slabs: Horizontal line (3 items)
     * - Stairs: Stair-like pattern (3 rows)
     * - Trapdoors: 2-item horizontal line
     * - Lanterns: Special pattern with base and secondary material
     *
     * @param registryLookup Provides access to game registries
     * @param recipeBlock The block being crafted
     * @param baseBlock The primary material used in crafting
     * @param exporter Handles exporting the generated recipe
     * @param amount Number of items produced by the recipe
     * @param secondaryMaterial Optional secondary material (e.g. used for lanterns)
     * @param recipeVariant Optional identifier for recipe variants
     */
    private fun createRecipe(
        registryLookup: RegistryWrapper.WrapperLookup,
        recipeBlock: Block,
        baseBlock: Item,
        exporter: RecipeExporter,
        amount: Int = 1,
        secondaryMaterial: ItemConvertible? = null,
        recipeVariant: String = ""
    ) {
        // Determine crafting pattern based on block type
        val pattern = when(recipeBlock) {
            is SlabBlock -> listOf("XXX")  // Horizontal line for slabs
            is StairsBlock -> listOf(      // Stair-like pattern
                "X  ",
                "XX ",
                "XXX"
            )
            is TrapdoorBlock -> listOf("XX")  // 2-item horizontal line
            is LanternBlock -> listOf("IX")   // Special lantern pattern
            else -> listOf("X")  // Fallback: single item
        }

        // Build the shaped recipe
        ShapedRecipeJsonBuilder.create(
            registryLookup.getOrThrow(RegistryKeys.ITEM),
            RecipeCategory.BUILDING_BLOCKS,
            recipeBlock,
            amount
        ).apply {
            // Apply crafting pattern
            pattern.forEach { patternLine ->
                pattern(patternLine)
            }

            // Configure recipe inputs
            if (secondaryMaterial != null) {
                input('X', baseBlock)      // Base material
                input('I', secondaryMaterial)  // Secondary material
            } else {
                input('X', baseBlock)      // Single material input
            }

            // Add recipe unlock criterion
            criterion(
                "has_${baseBlock.translationKey}",
                RecipeUnlockedCriterion.create(
                    RegistryKey.of(
                        RegistryKeys.RECIPE,
                        Identifier.of("${MODID}:${recipeBlock.translationKey}")
                    )
                )
            )

            // Export the recipe with a unique identifier
            offerTo(
                exporter,
                "${MODID}_${recipeBlock.translationKey}$recipeVariant"
            )
        }
    }

    /**
     * Provides the name of this recipe generator, typically used for logging and identification.
     *
     * @return The mod ID used as the generator's name
     */
    override fun getName(): String = MODID
}