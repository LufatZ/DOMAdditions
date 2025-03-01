package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry
import de.additions.blocks.RedstoneLantern
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
class RecipeGenerator(
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricRecipeProvider(output, registriesFuture) {
    lateinit var lookUp: RegistryWrapper.WrapperLookup
    lateinit var exporter: RecipeExporter

    /**
     * Overrides the default recipe generation method to provide custom recipe generation logic.
     *
     * @param registryLookup Provides access to game registries
     * @param exporter Handles exporting generated recipes
     * @return A custom RecipeGenerator that calls our specialized generation method
     */
    override fun getRecipeGenerator(
        registryLookup: RegistryWrapper.WrapperLookup,
        exporter: RecipeExporter,
    ): RecipeGenerator? {
        this.lookUp = registryLookup
        this.exporter = exporter
        // Create an anonymous RecipeGenerator that calls our custom generation method
        return object : RecipeGenerator(registryLookup, exporter) {
            override fun generate() {
                // Invoke method to generate recipes for various block types
                generateRecipes()
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
        // Generate stairs recipes
        stairsBlocks.forEachIndexed { index, stairBlock ->
            createBlockRecipe(
                stairBlock,
                baseBlocks[index].asItem(),
                4, // 4 stairs per craft
            )
        }

        // Generate slab recipes
        slabsBlocks.forEachIndexed { index, slabBlock ->
            createBlockRecipe(
                slabBlock,
                baseBlocks[index].asItem(),
                6, // 6 slabs per craft
            )
        }

        // Generate trapdoor recipes
        trapdoorBlocks.forEachIndexed { index, trapdoorBlock ->
            createBlockRecipe(
                trapdoorBlock,
                trapdoorParents[index].asItem(),
                1,
            )
        }

        // Generate lantern recipes from different base materials
        lanternBlocks.forEach { (lantern, baseBlock) ->
            // Lantern recipe from candle
            if (lantern !is RedstoneLantern) {
                val lanternSource: Map<Item, String> =
                    mapOf(
                        Items.CANDLE to "_from_candle",
                        Items.TORCH to "_from_torch",
                        Blocks.LANTERN.asItem() to "_from_lantern",
                    )
                for ((item, variant) in lanternSource) {
                    createBlockRecipe(
                        lantern,
                        item,
                        1,
                        baseBlock.asItem(),
                        variant,
                    )
                }
            } else {
                val sister: Block? =
                    BlockRegistry.registeredLanterns.entries
                        .find { (lantern1, baseBlock1) ->
                            lantern1 != lantern && baseBlock1 == baseBlock
                        }?.key ?: Blocks.LANTERN
                createBlockRecipe(
                    lantern,
                    Blocks.REDSTONE_WIRE.asItem(),
                    1,
                    sister,
                )
            }
        }

        // Generate recipes for mod items
        items.forEach { stack ->
            createItemRecipe(
                stack.item,
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
     * @param recipeBlock The block being crafted
     * @param baseBlock The primary material used in crafting
     * @param amount Number of items produced by the recipe
     * @param secondaryMaterial Optional secondary material (e.g. used for lanterns)
     * @param recipeVariant Optional identifier for recipe variants
     */
    private fun createBlockRecipe(
        recipeBlock: Block,
        baseBlock: Item,
        amount: Int = 1,
        secondaryMaterial: ItemConvertible? = null,
        recipeVariant: String = "",
    ) {
        // Determine crafting pattern based on block type
        val pattern =
            when (recipeBlock) {
                is SlabBlock -> listOf("XXX") // Horizontal line for slabs
                is StairsBlock ->
                    listOf( // Stair-like pattern
                        "X  ",
                        "XX ",
                        "XXX",
                    )
                is TrapdoorBlock -> listOf("XX") // 2-item horizontal line
                is LanternBlock -> listOf("IX") // Special lantern pattern
                else -> listOf("X") // Fallback: single item
            }

        // Build the shaped recipe
        ShapedRecipeJsonBuilder
            .create(
                lookUp.getOrThrow(RegistryKeys.ITEM),
                RecipeCategory.BUILDING_BLOCKS,
                recipeBlock,
                amount,
            ).apply {
                // Apply crafting pattern
                pattern.forEach { patternLine ->
                    pattern(patternLine)
                }

                // Configure recipe inputs
                if (secondaryMaterial != null) {
                    input('X', baseBlock) // Base material
                    input('I', secondaryMaterial) // Secondary material
                } else {
                    input('X', baseBlock) // Single material input
                }

                // Add recipe unlock criterion
                criterion(
                    "has_${baseBlock.translationKey.replaceBeforeLast('.', "").replace(".", "")}",
                    InventoryChangedCriterion.Conditions.items(
                        ItemPredicate.Builder
                            .create()
                            .items(lookUp.getOrThrow(RegistryKeys.ITEM), baseBlock)
                            .build(),
                    ),
                )

                // Export the recipe with a unique identifier
                offerTo(
                    exporter,
                    RegistryKey.of(
                        RegistryKeys.RECIPE,
                        Identifier.of(MODID, recipeBlock.translationKey.replace(".", "_") + recipeVariant),
                    ),
                )
            }
    }

    private fun createItemRecipe(item: Item) {
        // Lambda, das den Fallback erzeugt und den Logeintrag schreibt
        val eFallbackShape: () -> List<String> = {
            logger.warn("Falling back to single item recipe, because no shape is defined for this item. ($item)")
            listOf("X")
        }
        val eMaterialComponents: () -> Nothing? = {
            logger.warn("Fallback -> Unknown material for additional recipe components. ($item)")
            null
        }

        val shape =
            when (item) {
                is RadiusMineItem ->
                    when (item.effectiveBlocks.id) {
                        BlockTags.SHOVEL_MINEABLE.id -> listOf("XSX", "MSM", "XMX")
                        BlockTags.PICKAXE_MINEABLE.id -> listOf("MMX", "MSX", "XSX")
                        else -> eFallbackShape()
                    }
                else -> eFallbackShape()
            }
        val additionalMaterial =
            when (item) {
                is RadiusMineItem ->
                    when (item.material) {
                        C_WOOD -> Ingredient.ofItems(Items.STRING)
                        C_STONE -> Ingredient.ofItems(Items.DEEPSLATE)
                        C_IRON -> Ingredient.ofItems(Items.COPPER_BLOCK)
                        C_DIAMOND -> Ingredient.ofItems(Items.AMETHYST_BLOCK)
                        C_GOLD -> Ingredient.ofItems(Items.GOLD_INGOT)
                        C_NETHERITE -> Ingredient.ofItems(Items.CRYING_OBSIDIAN)
                        else -> eMaterialComponents()
                    }
                else -> null
            }

        if (item is RadiusMineItem) {
            val itemLookup = lookUp.getOrThrow(RegistryKeys.ITEM)

            ShapedRecipeJsonBuilder
                .create(
                    itemLookup,
                    RecipeCategory.TOOLS,
                    item,
                    1,
                ).apply {
                    shape.forEach { patternLine ->
                        pattern(patternLine)
                    }
                    input('M', item.getMaterialIngredient(itemLookup))
                    input('S', Items.STICK)

                    if (additionalMaterial != null) {
                        input('X', additionalMaterial)
                    }

                    group("radius_mine")

                    // material criterion
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
                    // recipe unlock criterion
                    criterion(
                        "has_recipe_${item.translationKey.replaceBeforeLast('.',"").replace(".","")}",
                        RecipeUnlockedCriterion.create(
                            @Suppress("ktlint:standard:max-line-length")
                            RegistryKey.of(
                                RegistryKeys.RECIPE,
                                Identifier.of(MODID, item.translationKey.replace(".", "_")), // Muss mit recipe_id in offerTo() übereinstimmen
                            ),
                        ),
                    )

                    offerTo(
                        exporter,
                        RegistryKey.of( // Konsistente RegistryKey-Erstellung
                            RegistryKeys.RECIPE,
                            Identifier.of(MODID, item.translationKey.replace(".", "_")),
                        ),
                    )
                }
        } else {
            logger.warn(
                "Cant create recipe for $item, because there is no preset for it. If this Item can´t be crafted, ignore this warning.",
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
