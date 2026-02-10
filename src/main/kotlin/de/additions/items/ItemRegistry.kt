package de.additions.items

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.Additions.logging
import de.additions.itemGroups.ItemGroupRegistry
import de.additions.items.ToolItem.Companion.MiningTypes
import de.additions.items.ToolItem.Companion.RADIUS
import de.additions.items.ToolItem.Companion.ToolTypes
import de.additions.items.ToolItem.Companion.materials
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.LoreComponent
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

/**
 * A singleton registry responsible for registering custom tool items with material variants.
 *
 * Handles the generation and registration of custom multi-block tools (e.g., shovels, hammers,
 * axes, pickaxes) that support radius-based or vein-based mining behaviors.
 * Each tool is instantiated per material defined in [materials] and configured with
 * material-specific properties such as attack damage, attack speed, mining behavior,
 * effective block tags, and descriptive tooltips.
 *
 * In addition to item registration, this registry keeps track of all created tools
 * for use in item group placement and data generation (e.g., models, recipes).
 */
object ItemRegistry {

    /**
     * A mutable list containing [ItemStack] instances of all successfully registered tools.
     *
     * This collection serves multiple purposes:
     * - Placement of items into item groups
     * - Data generation (e.g., models, recipes, loot tables)
     *
     * The list is populated during item registration and represents the complete
     * set of tools added by this mod.
     */
    val registeredItems: MutableList<ItemStack> = mutableListOf()

    /**
     * Registers all custom tool items and adds them to the appropriate item groups.
     *
     * This includes tools with radius-area mining and vein mining behaviors.
     * After registration, all items are inserted into the default item groups
     * relative to [Items.DIAMOND_PICKAXE].
     */
    fun registerItems() {
        logger.info("Adding items")
        addToolItems()
        logger.info("Registering added items")
        logger.info("Finished item registration with ${registeredItems.size} items")
        ItemGroupRegistry.registerItemsAfterCommonParent(registeredItems, Items.DIAMOND_PICKAXE)
    }

    /**
     * Registers tool items for each material defined in [materials].
     *
     * For each material, a tool item is created, configured, and registered using
     * the provided parameters.
     *
     * @param placeholderId Base item ID pattern containing "%material", which is replaced
     * with the material name.
     * @param tooltip LoreComponent applied to all generated tools.
     * @param damage Base attack damage of the tool.
     * @param speed Attack speed modifier of the tool.
     * @param type The base tool type (e.g., SHOVEL, PICKAXE, AXE, HOE), determining vanilla
     * behavior and effective block tags.
     * @param miningType Custom mining behavior defining whether the tool uses
     * radius-area mining or vein mining logic.
     */
    private fun registerToolItems(
        placeholderId: String,
        tooltip: LoreComponent,
        damage: Float,
        speed: Float,
        type: ToolTypes,
        miningType: MiningTypes,
    ) {
        materials.forEach { (materialName, material) ->
            runCatching {
                val id = placeholderId.replace("%material", materialName)

                val toolFunction = when (type) {
                    ToolTypes.SHOVEL -> Item.Settings::shovel
                    ToolTypes.PICKAXE -> Item.Settings::pickaxe
                    ToolTypes.AXE -> Item.Settings::axe
                    ToolTypes.HOE -> Item.Settings::hoe
                }

                val toolClass = when (miningType) {
                    MiningTypes.RADIUS_MINING -> ::RadiusMineItem
                    MiningTypes.VEIN_MINING -> ::VeinMineItem
                }

                val effectiveBlocks = when (type) {
                    ToolTypes.SHOVEL -> BlockTags.SHOVEL_MINEABLE
                    ToolTypes.PICKAXE -> BlockTags.PICKAXE_MINEABLE
                    ToolTypes.AXE -> BlockTags.AXE_MINEABLE
                    ToolTypes.HOE -> BlockTags.HOE_MINEABLE
                }

                val toolSettings = toolFunction(Item.Settings(), material, damage, speed)
                    .component(DataComponentTypes.LORE, tooltip)

                val registryKey =
                    RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, id))

                val tool =
                    toolClass(material, effectiveBlocks, toolSettings.registryKey(registryKey))

                Registry.register(
                    Registries.ITEM,
                    RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, id)),
                    tool
                )

                registeredItems.add(ItemStack(tool))

                if (logging) {
                    logger.info("registered tool item: $id")
                }
            }.onFailure { e ->
                logger.error("Failed to register tool items for material: $materialName")
                logger.error(e.stackTraceToString())
            }
        }
    }

    /**
     * Registers all custom multi-block tools with material-specific configurations.
     *
     * Tool behaviors:
     * - Shovels support radius-area mining and path creation.
     * - Hammers support radius-area mining and path creation.
     * - Axes support vein mining for efficient tree harvesting and wood stripping.
     * - Pickaxes support vein mining for ore extraction.
     *
     * Tool stats such as attack damage and attack speed are adjusted to balance
     * the increased efficiency of these tools.
     */
    private fun addToolItems() {
        val diameter = 2 * RADIUS + 1
        val areaText = Text.literal("${diameter}x${diameter}").formatted(Formatting.GRAY)

        val shovelLore =
            LoreComponent(
                listOf(
                    Text.translatable(
                        "tooltip.additions.radius_mine.shovel_description",
                        areaText
                    ).formatted(Formatting.WHITE),
                    Text.translatable(
                        "tooltip.additions.radius_mine.path_creation_description"
                    ).formatted(Formatting.WHITE),
                    Text.translatable(
                        "tooltip.additions.radius_mine.sneak_description"
                    ).formatted(Formatting.GRAY),
                ),
            )

        val hammerLore =
            LoreComponent(
                listOf(
                    Text.translatable(
                        "tooltip.additions.radius_mine.hammer_description",
                        areaText
                    ).formatted(Formatting.WHITE),
                    Text.translatable(
                        "tooltip.additions.radius_mine.path_creation_description"
                    ).formatted(Formatting.WHITE),
                    Text.translatable(
                        "tooltip.additions.radius_mine.sneak_description"
                    ).formatted(Formatting.GRAY),
                ),
            )

        val axeLore =
            LoreComponent(
                listOf(
                    Text.translatable(
                        "tooltip.additions.radius_mine.axe_description",
                        areaText
                    ).formatted(Formatting.WHITE),
                    Text.translatable(
                        "tooltip.additions.radius_mine.stripped_wood_creation_description"
                    ).formatted(Formatting.WHITE),
                ),
            )

        val pickaxeLore =
            LoreComponent(
                listOf(
                    Text.translatable(
                        "tooltip.additions.radius_mine.pickaxe_description",
                        areaText
                    ).formatted(Formatting.WHITE),
                ),
            )

        registerToolItems(
            "%material_terraformer_shovel",
            shovelLore,
            1.5f,
            -3.0f,
            ToolTypes.SHOVEL,
            MiningTypes.RADIUS_MINING,
        )

        registerToolItems(
            "%material_hammer",
            hammerLore,
            1f,
            -2.8f,
            ToolTypes.PICKAXE,
            MiningTypes.RADIUS_MINING
        )

        registerToolItems(
            "%material_lumberjack_axe",
            axeLore,
            6f,
            -3.2f,
            ToolTypes.AXE,
            MiningTypes.VEIN_MINING,
        )

        registerToolItems(
            "%material_prospector_pickaxe",
            pickaxeLore,
            1.5f,
            -3.0f,
            ToolTypes.PICKAXE,
            MiningTypes.VEIN_MINING,
        )
    }
}