@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.blocks.*
import de.additions.items.ItemRegistry
import de.additions.items.RadiusMineItem
import de.additions.items.RadiusMineItem.Companion.C_DIAMOND
import de.additions.items.RadiusMineItem.Companion.C_GOLD
import de.additions.items.RadiusMineItem.Companion.C_IRON
import de.additions.items.RadiusMineItem.Companion.C_NETHERITE
import de.additions.items.RadiusMineItem.Companion.C_STONE
import de.additions.items.RadiusMineItem.Companion.C_WOOD
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancement.criterion.InventoryChangedCriterion
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
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.recipe.Ingredient
import net.minecraft.recipe.book.RecipeCategory
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

/**
 * AdditionsRecipeGenerator is responsible for automatically creating crafting recipes
 * for various block variants and custom items in the Additions mod.
 *
 * Key Responsibilities:
 * - Generate recipes for decorative blocks (stairs, slabs, trapdoors)
 * - Create recipes for normal and redstone lantern variants
 * - Generate tool recipes for RadiusMineItem instances
 * - Handle recipe variants with different base materials
 * - Create vanilla override recipes like the name tag
 *
 * @property output FabricDataOutput for writing generated recipe files
 * @property registriesFuture Provides access to game registries during recipe generation
 */
class RecipeGenerator(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricRecipeProvider(output, registriesFuture) {
    // Registry lookup and exporter instances
    private lateinit var lookUp: RegistryWrapper.WrapperLookup
    private lateinit var exporter: RecipeExporter

    /**
     * Overrides the default recipe generation method to provide custom recipe generation logic.
     *
     * @param registryLookup Provides access to game registries
     * @param exporter Handles exporting generated recipes
     * @return A custom RecipeGenerator that uses our specialized generation methods
     */
    override fun getRecipeGenerator(
        registryLookup: RegistryWrapper.WrapperLookup,
        exporter: RecipeExporter,
    ): RecipeGenerator {
        this.lookUp = registryLookup
        this.exporter = exporter

        // Create anonymous RecipeGenerator that calls our custom generation method
        return object : RecipeGenerator(registryLookup, exporter) {
            override fun generate() {
                generateRecipes()
            }
        }
    }

    /**
     * Main recipe generation method that coordinates creation of all mod recipes.
     * Processes each block type and generates appropriate crafting recipes.
     */
    private fun generateRecipes() {
        // Retrieve registered blocks from BlockRegistry
        val stairsBlocks = BlockRegistry.registeredStairs
        val baseBlocks = BlockRegistry.blockVariantsParents
        val slabsBlocks = BlockRegistry.registeredSlabs
        val trapdoorBlocks = BlockRegistry.registeredTrapdoors
        val trapdoorParents = BlockRegistry.trapdoorVariantsParents
        val lanternBlocks = BlockRegistry.registeredLanterns
        val items = ItemRegistry.registeredItems

        // Generate stairs recipes (4 stairs per craft)
        stairsBlocks.forEachIndexed { index, stairBlock ->
            createBlockRecipe(
                stairBlock,
                baseBlocks[index].asItem(),
                4,
            )
        }

        // Generate slab recipes (6 slabs per craft)
        slabsBlocks.forEachIndexed { index, slabBlock ->
            createBlockRecipe(
                slabBlock,
                baseBlocks[index].asItem(),
                6,
            )
        }

        // Generate trapdoor recipes (1 trapdoor per craft)
        trapdoorBlocks.forEachIndexed { index, trapdoorBlock ->
            createBlockRecipe(
                trapdoorBlock,
                trapdoorParents[index].asItem(),
                2,
            )
        }

        // Generate lantern recipes with multiple base material options
        lanternBlocks.forEach { (lantern, baseBlock) ->
            if (lantern !is RedstoneLantern) {
                // Normal lanterns can be crafted from candles, torches, or lanterns
                generateNormalLanternRecipes(lantern, baseBlock)
            } else {
                // Redstone lanterns are crafted from their normal counterparts
                generateRedstoneLanternRecipe(lantern, baseBlock)
            }
        }

        // Generate recipes for mod items
        items.forEach { stack ->
            createItemRecipe(stack.item)
        }

        // Generate vanilla override recipes
        createNameTagRecipe()
    }

    /**
     * Creates a custom recipe for the name tag item.
     * Pattern:
     * " #i"
     * "#O#"
     * "P# "
     * Where:
     * # - String
     * i - Iron Nugget
     * O - Any Sign (from the signs tag)
     * P - Paper
     */
    private fun createNameTagRecipe() {
        // Get item registry lookup
        val itemLookup = lookUp.getOrThrow(RegistryKeys.ITEM)

        // Create shaped recipe for the name tag
        ShapedRecipeJsonBuilder
            .create(
                itemLookup,
                RecipeCategory.TOOLS,
                Items.NAME_TAG,
                1,
            ).apply {
                // Apply the crafting pattern
                pattern(" #i")
                pattern("#O#")
                pattern("P# ")

                // Configure recipe inputs
                input('#', Items.STRING)
                input('i', Items.IRON_NUGGET)
                input('O', ItemTags.SIGNS)
                input('P', Items.PAPER)

                // Add recipe unlock criterion based on having the primary materials
                criterion(
                    "has_string",
                    InventoryChangedCriterion.Conditions.items(
                        ItemPredicate.Builder
                            .create()
                            .items(itemLookup, Items.STRING)
                            .build(),
                    ),
                )

                criterion(
                    "has_paper",
                    InventoryChangedCriterion.Conditions.items(
                        ItemPredicate.Builder
                            .create()
                            .items(itemLookup, Items.PAPER)
                            .build(),
                    ),
                )

                // Create a unique recipe identifier
                val recipeId = Identifier.of(MODID, "name_tag")

                // Export the recipe
                offerTo(
                    exporter,
                    RegistryKey.of(RegistryKeys.RECIPE, recipeId),
                )
            }
    }

    /**
     * Generates recipes for normal lantern variants from different source materials.
     *
     * @param lantern The lantern block being crafted
     * @param baseBlock The material block used as base
     */
    private fun generateNormalLanternRecipes(
        lantern: Block,
        baseBlock: Block,
    ) {
        val lanternSources =
            mapOf(
                Items.CANDLE to "_from_candle",
                Items.TORCH to "_from_torch",
                Blocks.LANTERN.asItem() to "_from_lantern",
            )
        val secondaryMaterial =
            when (baseBlock) {
                Blocks.NETHERITE_BLOCK -> Items.NETHERITE_INGOT
                Blocks.COPPER_BLOCK -> Items.COPPER_INGOT
                Blocks.IRON_BLOCK -> Items.IRON_INGOT
                Blocks.GOLD_BLOCK -> Items.GOLD_INGOT
                Blocks.DIAMOND_BLOCK -> Items.DIAMOND
                Blocks.EMERALD_BLOCK -> Items.EMERALD
                Blocks.AMETHYST_BLOCK -> Items.AMETHYST_SHARD
                else -> baseBlock.asItem()
            }
        for ((sourceItem, variantSuffix) in lanternSources) {
            createBlockRecipe(
                lantern,
                sourceItem,
                1,
                secondaryMaterial,
                variantSuffix,
            )
        }
    }

    /**
     * Generates recipes for redstone lantern variants from their normal counterparts.
     *
     * @param lantern The redstone lantern block being crafted
     * @param baseBlock The material block used as base
     */
    private fun generateRedstoneLanternRecipe(
        lantern: Block,
        baseBlock: Block,
    ) {
        // Determine the corresponding normal lantern class based on redstone lantern type
        val targetClass =
            when (lantern) {
                is SmallRedstoneLantern -> SmallLantern::class.java
                is BigRedstoneLantern -> BigLantern::class.java
                else -> LanternBlock::class.java
            }

        // Find the "sister" non-redstone lantern with matching base material
        val sisterLantern = findSisterLantern(lantern, baseBlock, targetClass)

        // Create recipe using the sister lantern and redstone
        createBlockRecipe(
            lantern,
            Items.REDSTONE,
            1,
            sisterLantern,
        )
    }

    /**
     * Finds the corresponding "sister" lantern of the opposite type
     * (normal vs. redstone) with the same base material.
     *
     * @param lantern The current lantern
     * @param baseBlock The base material block
     * @param targetClass The class of the lantern type to find
     * @return The matching sister lantern or default lantern if not found
     */
    private fun findSisterLantern(
        lantern: Block,
        baseBlock: Block,
        targetClass: Class<*>,
    ): Block =
        BlockRegistry.registeredLanterns.entries
            .find { (candidateLantern, candidateBaseBlock) ->
                candidateLantern != lantern &&
                    targetClass.isInstance(candidateLantern) &&
                    candidateBaseBlock == baseBlock
            }?.key ?: Blocks.LANTERN

    /**
     * Creates a shaped recipe for a block with configurable parameters.
     *
     * @param recipeBlock The block being crafted
     * @param baseBlock The primary material used in crafting
     * @param amount Number of items produced by the recipe
     * @param secondaryMaterial Optional secondary material (e.g., for lanterns)
     * @param recipeVariant Optional identifier suffix for recipe variants
     */
    private fun createBlockRecipe(
        recipeBlock: Block,
        baseBlock: Item,
        amount: Int = 1,
        secondaryMaterial: ItemConvertible? = null,
        recipeVariant: String = "",
    ) {
        // Determine crafting pattern based on block type
        // The order is important here, as Small/BigLantern are also instances of LanternBlock.
        val pattern =
            when (recipeBlock) {
                is SlabBlock -> listOf("XXX") // Horizontal line for slabs
                is StairsBlock ->
                    listOf( // Stair-like pattern
                        "X  ",
                        "XX ",
                        "XXX",
                    )
                is TrapdoorBlock -> listOf("XXX", "XXX") // 2-item horizontal line for trapdoors
                // Redstone lanterns are a simple upgrade: normal lantern + redstone. The "IX" pattern creates a shapeless-like 2-item recipe.
                // Custom lanterns with different sizes need unique recipes to avoid conflicts.
                // 'I' is the frame material (e.g., iron ingot), 'X' is the light source (e.g., torch).
                is SmallRedstoneLantern, is BigRedstoneLantern, is RedstoneLantern, is SmallLantern -> listOf("IX")
                is BigLantern -> listOf("XI")
                is LanternBlock -> listOf("X", "I")
                else -> listOf("X") // Fallback: single item
            }

        // Get registry key for items
        val itemRegistry = lookUp.getOrThrow(RegistryKeys.ITEM)

        // Build the shaped recipe
        ShapedRecipeJsonBuilder
            .create(
                itemRegistry,
                RecipeCategory.BUILDING_BLOCKS,
                recipeBlock,
                amount,
            ).apply {
                // Apply the crafting pattern
                pattern.forEach { patternLine ->
                    pattern(patternLine)
                }

                // Configure recipe inputs
                input('X', baseBlock) // Base material
                if (secondaryMaterial != null) {
                    input('I', secondaryMaterial) // Secondary material
                }

                // Create a clean identifier for the criterion
                val criterionId = "has_${baseBlock.translationKey.replaceBeforeLast('.', "").replace(".", "")}"

                // Add recipe unlock criterion based on having the base material
                criterion(
                    criterionId,
                    InventoryChangedCriterion.Conditions.items(
                        ItemPredicate.Builder
                            .create()
                            .items(itemRegistry, baseBlock)
                            .build(),
                    ),
                )

                // Create a unique recipe identifier
                val recipeId = Identifier.of(MODID, recipeBlock.translationKey.replace(".", "_") + recipeVariant)

                // Export the recipe
                offerTo(
                    exporter,
                    RegistryKey.of(RegistryKeys.RECIPE, recipeId),
                )
            }
    }

    /**
     * Creates recipes for special items like radius mining tools.
     * Different tools have different crafting patterns and materials.
     *
     * @param item The item to create a recipe for
     */
    private fun createItemRecipe(item: Item) {
        // Skip if not a supported item type
        if (item !is RadiusMineItem) {
            logger.warn(
                "Can't create recipe for $item because there is no preset for it. " +
                    "If this item can't be crafted, ignore this warning.",
            )
            return
        }

        // Get crafting pattern based on tool type
        val shape = getItemRecipeShape(item)

        // Get additional material component based on tool material
        val additionalMaterial = getAdditionalMaterial(item)

        // Get item registry lookup
        val itemLookup = lookUp.getOrThrow(RegistryKeys.ITEM)

        // Create shaped recipe for the radius mining tool
        ShapedRecipeJsonBuilder
            .create(
                itemLookup,
                RecipeCategory.TOOLS,
                item,
                1,
            ).apply {
                // Apply crafting pattern
                shape.forEach { patternLine ->
                    pattern(patternLine)
                }

                // Configure recipe inputs
                input('M', item.getMaterialIngredient(itemLookup))
                input('S', Items.STICK)
                if (additionalMaterial != null) {
                    input('X', additionalMaterial)
                }

                // Group similar recipes together
                group("radius_mine")

                // Add material criterion
                addMaterialCriterion(item, itemLookup)

                // Add recipe unlock criterion
                addRecipeUnlockCriterion(item)

                // Generate recipe ID and export
                val recipeId = Identifier.of(MODID, item.translationKey.replace(".", "_"))
                offerTo(
                    exporter,
                    RegistryKey.of(RegistryKeys.RECIPE, recipeId),
                )
            }
    }

    /**
     * Determines the crafting pattern for a special item.
     *
     * @param item The item to get a recipe shape for
     * @return List of strings representing the crafting pattern
     */
    private fun getItemRecipeShape(item: Item): List<String> {
        if (item is RadiusMineItem) {
            return when (item.effectiveBlocks.id) {
                BlockTags.SHOVEL_MINEABLE.id -> listOf("XSX", "MSM", "XMX") // Shovel pattern
                BlockTags.PICKAXE_MINEABLE.id -> listOf("XMX", "MSM", "XSX") // Pickaxe pattern
                else -> {
                    logger.warn("Falling back to single item recipe, because no shape is defined for ${item.effectiveBlocks.id}")
                    listOf("X") // Fallback pattern
                }
            }
        }

        logger.warn("Falling back to single item recipe, because no shape is defined for this item. ($item)")
        return listOf("X") // Default fallback
    }

    /**
     * Determines the additional crafting material for special items based on their material tier.
     *
     * @param item The item to get additional material for
     * @return Ingredient representing the additional crafting material
     */
    private fun getAdditionalMaterial(item: Item): Ingredient? {
        if (item is RadiusMineItem) {
            return when (item.material) {
                C_WOOD -> Ingredient.ofItems(Items.STRING)
                C_STONE -> Ingredient.ofItems(Items.DEEPSLATE)
                C_IRON -> Ingredient.ofItems(Items.COPPER_BLOCK)
                C_DIAMOND -> Ingredient.ofItems(Items.AMETHYST_BLOCK)
                C_GOLD -> Ingredient.ofItems(Items.GOLD_BLOCK)
                C_NETHERITE -> Ingredient.ofItems(Items.CRYING_OBSIDIAN)
                else -> {
                    logger.warn("Unknown material for additional recipe components: ${item.material}")
                    null
                }
            }
        }
        return null
    }

    /**
     * Adds a crafting criterion based on having the material used in the tool.
     *
     * @param item The RadiusMineItem to add criterion for
     * @param itemLookup The item registry lookup
     */
    private fun ShapedRecipeJsonBuilder.addMaterialCriterion(
        item: RadiusMineItem,
        itemLookup: RegistryWrapper<Item>,
    ) {
        criterion(
            "has_${item.getMaterialName()}",
            InventoryChangedCriterion.Conditions.items(
                item.getCraftingTagOrItem().let { (tag, craftingItem) ->
                    ItemPredicate.Builder
                        .create()
                        .apply {
                            when {
                                tag != null -> tag(itemLookup, tag)
                                craftingItem != null -> items(itemLookup, craftingItem)
                                else -> items(itemLookup, Items.STICK)
                            }
                        }.build()
                },
            ),
        )
    }

    /**
     * Adds a recipe unlock criterion for the item.
     *
     * @param item The item to add a recipe unlock criterion for
     */
    private fun ShapedRecipeJsonBuilder.addRecipeUnlockCriterion(item: Item) {
        val criterionId = "has_recipe_${item.translationKey.replaceBeforeLast('.', "").replace(".", "")}"
        val recipeId = Identifier.of(MODID, item.translationKey.replace(".", "_"))

        criterion(
            criterionId,
            RecipeUnlockedCriterion.create(
                RegistryKey.of(RegistryKeys.RECIPE, recipeId),
            ),
        )
    }

    /**
     * Provides the name of this recipe generator for identification purposes.
     *
     * @return The mod ID used as the generator's name
     */
    override fun getName(): String = MODID
}
