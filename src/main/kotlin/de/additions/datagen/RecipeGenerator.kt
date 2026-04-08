@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.blocks.*
import de.additions.blocks.lanterns.*
import de.additions.items.*
import de.additions.items.ToolItem.Companion.C_DIAMOND
import de.additions.items.ToolItem.Companion.C_GOLD
import de.additions.items.ToolItem.Companion.C_IRON
import de.additions.items.ToolItem.Companion.C_NETHERITE
import de.additions.items.ToolItem.Companion.C_STONE
import de.additions.items.ToolItem.Companion.C_WOOD
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancements.criterion.InventoryChangeTrigger
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.*
import java.util.concurrent.CompletableFuture

/**
 * AdditionsRecipeGenerator is responsible for automatically creating crafting recipes
 * for various block variants and custom items in the Additions mod.
 *
 * Key Responsibilities:
 * - Generate recipes for decorative blocks (stairs, slabs, trapdoors)
 * - Create recipes for normal and redstone lantern variants
 * - Generate tool recipes for ToolItem instances
 * - Handle recipe variants with different base materials
 * - Create vanilla override recipes like the name tag
 *
 * @property output FabricDataOutput for writing generated recipe files
 * @property registriesFuture Provides access to game registries during recipe generation
 */
class RecipeGenerator(
    output: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricRecipeProvider(output, registriesFuture) {
    // Registry lookup and exporter instances
    private lateinit var lookUp: HolderLookup.Provider
    private lateinit var exporter: RecipeOutput

    /**
     * Overrides the default recipe generation method to provide custom recipe generation logic.
     *
     * @param registryLookup Provides access to game registries
     * @param exporter Handles exporting generated recipes
     * @return A custom RecipeGenerator that uses our specialized generation methods
     */
    override fun createRecipeProvider(
        registryLookup: HolderLookup.Provider,
        exporter: RecipeOutput,
    ): RecipeProvider {
        this.lookUp = registryLookup
        this.exporter = exporter

        // Create anonymous RecipeGenerator that calls our custom generation method
        return object : RecipeProvider(registryLookup, exporter) {
            override fun buildRecipes() {
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
        items.forEach { item ->
            createItemRecipe(item)
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
        val itemLookup = lookUp.lookupOrThrow(Registries.ITEM)

        // Create shaped recipe for the name tag
        ShapedRecipeBuilder
            .shaped(
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
                define('#', Items.STRING)
                define('i', Items.IRON_NUGGET)
                define('O', ItemTags.SIGNS)
                define('P', Items.PAPER)

                // Add recipe unlock criterion based on having the primary materials
                unlockedBy(
                    "has_string",
                    InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder
                            .item()
                            .of(itemLookup, Items.STRING)
                            .build(),
                    ),
                )

                unlockedBy(
                    "has_paper",
                    InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder
                            .item()
                            .of(itemLookup, Items.PAPER)
                            .build(),
                    ),
                )

                // Create a unique recipe identifier
                val recipeId = Identifier.fromNamespaceAndPath(MODID, "name_tag")

                // Export the recipe
                save(
                    exporter,
                    ResourceKey.create(Registries.RECIPE, recipeId),
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
        secondaryMaterial: ItemLike? = null,
        recipeVariant: String = "",
    ) {
        // Determine crafting pattern based on block type
        // The order is important here, as Small/BigLantern are also instances of LanternBlock.
        val pattern =
            when (recipeBlock) {
                is SlabBlock -> listOf("XXX") // Horizontal line for slabs
                is StairBlock ->
                    listOf( // Stair-like pattern
                        "X  ",
                        "XX ",
                        "XXX",
                    )
                is TrapDoorBlock -> listOf("XXX", "XXX") // 2-item horizontal line for trapdoors
                // Redstone lanterns are a simple upgrade: normal lantern + redstone. The "IX" pattern creates a shapeless-like 2-item recipe.
                // Custom lanterns with different sizes need unique recipes to avoid conflicts.
                // 'I' is the frame material (e.g., iron ingot), 'X' is the light source (e.g., torch).
                is SmallRedstoneLantern, is BigRedstoneLantern, is RedstoneLantern, is SmallLantern -> listOf("IX")
                is BigLantern -> listOf("XI")
                is LanternBlock -> listOf("X", "I")
                else -> listOf("X") // Fallback: single item
            }

        // Get registry key for items
        val itemRegistry = lookUp.lookupOrThrow(Registries.ITEM)

        // Build the shaped recipe
        ShapedRecipeBuilder
            .shaped(
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
                define('X', baseBlock) // Base material
                if (secondaryMaterial != null) {
                    define('I', secondaryMaterial) // Secondary material
                }

                // Create a clean identifier for the criterion
                val criterionId = "has_${baseBlock.descriptionId.replaceBeforeLast('.', "").replace(".", "")}"

                // Add recipe unlock criterion based on having the base material
                unlockedBy(
                    criterionId,
                    InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder
                            .item()
                            .of(itemRegistry, baseBlock)
                            .build(),
                    ),
                )

                // Create a unique recipe identifier
                val recipeId = Identifier.fromNamespaceAndPath(MODID, recipeBlock.descriptionId.replace(".", "_") + recipeVariant)

                // Export the recipe
                save(
                    exporter,
                    ResourceKey.create(Registries.RECIPE, recipeId),
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
        if (item !is ToolItem) {
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
        val itemLookup = lookUp.lookupOrThrow(Registries.ITEM)

        // Create shaped recipe for mining tools
        ShapedRecipeBuilder
            .shaped(
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
                define('M', item.getMaterialIngredient(itemLookup))
                define('S', Items.STICK)
                if (additionalMaterial != null) {
                    define('X', additionalMaterial)
                }

                // Group similar recipes together
                group("radius_mine")

                // Add material criterion
                addMaterialCriterion(item, itemLookup)

                // Add recipe unlock criterion
                addRecipeUnlockCriterion(item)

                // Generate recipe ID and export
                val recipeId = Identifier.fromNamespaceAndPath(MODID, item.descriptionId.replace(".", "_"))
                save(
                    exporter,
                    ResourceKey.create(Registries.RECIPE, recipeId),
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
        if (item is ToolItem) {
            return when (item.effectiveBlocks.location) {
                BlockTags.MINEABLE_WITH_SHOVEL.location -> listOf(" S ", "MSM", "XMX") // Shovel pattern
                BlockTags.MINEABLE_WITH_PICKAXE.location ->
                    if (item is VeinMineItem) listOf("XMX", "MSM", " S ") // Pickaxe pattern
                    else listOf("MMM", "XSX", " S ")
                BlockTags.MINEABLE_WITH_AXE.location -> listOf("MMX", "MSX", " S ") // Pickaxe pattern
                else -> {
                    logger.warn("Falling back to single item recipe, because no shape is defined for ${item.effectiveBlocks.location}")
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
        if (item is ToolItem) {
            return when (item.material) {
                C_WOOD -> Ingredient.of(Items.STRING)
                C_STONE -> Ingredient.of(Items.LEAD)
                C_IRON -> Ingredient.of(Items.AMETHYST_SHARD)
                C_DIAMOND -> Ingredient.of(Items.QUARTZ)
                C_GOLD -> Ingredient.of(Items.QUARTZ)
                C_NETHERITE -> Ingredient.of(Items.NETHERITE_SCRAP)
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
     * @param item The ToolItem to add criterion for
     * @param itemLookup The item registry lookup
     */
    private fun ShapedRecipeBuilder.addMaterialCriterion(
        item: ToolItem,
        itemLookup: HolderLookup<Item>,
    ) {
        unlockedBy(
            "has_${item.getMaterialName()}",
            InventoryChangeTrigger.TriggerInstance.hasItems(
                item.getCraftingTagOrItem().let { (tag, craftingItem) ->
                    ItemPredicate.Builder
                        .item()
                        .apply {
                            when {
                                tag != null -> of(itemLookup, tag)
                                craftingItem != null -> of(itemLookup, craftingItem)
                                else -> of(itemLookup, Items.STICK)
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
    private fun ShapedRecipeBuilder.addRecipeUnlockCriterion(item: Item) {
        val criterionId = "has_recipe_${item.descriptionId.replaceBeforeLast('.', "").replace(".", "")}"
        val recipeId = Identifier.fromNamespaceAndPath(MODID, item.descriptionId.replace(".", "_"))

        unlockedBy(
            criterionId,
            RecipeUnlockedTrigger.unlocked(
                ResourceKey.create(Registries.RECIPE, recipeId),
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
