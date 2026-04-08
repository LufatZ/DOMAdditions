package de.additions.items

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.Additions.logging
import de.additions.itemGroups.ItemGroupRegistry
import de.additions.items.ToolItem.Companion.RADIUS
import de.additions.items.ToolItem.Companion.materials
import net.minecraft.ChatFormatting
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.ItemLore

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

    internal enum class ToolTypes { PICKAXE, AXE, SHOVEL, HOE }
    internal enum class MiningTypes { RADIUS_MINING, VEIN_MINING }
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
    val registeredItems: MutableList<Item> = mutableListOf()

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
        tooltip: ItemLore,
        damage: Float,
        speed: Float,
        type: ToolTypes,
        miningType: MiningTypes,
    ) {
        materials.forEach { (materialName, material) ->
            runCatching {
                val id = placeholderId.replace("%material", materialName)

                val toolFunction = when (type) {
                    ToolTypes.SHOVEL -> Item.Properties::shovel
                    ToolTypes.PICKAXE -> Item.Properties::pickaxe
                    ToolTypes.AXE -> Item.Properties::axe
                    ToolTypes.HOE -> Item.Properties::hoe
                }

                val toolClass = when (miningType) {
                    MiningTypes.RADIUS_MINING -> ::RadiusMineItem
                    MiningTypes.VEIN_MINING -> ::VeinMineItem
                }

                val effectiveBlocks = when (type) {
                    ToolTypes.SHOVEL -> BlockTags.MINEABLE_WITH_SHOVEL
                    ToolTypes.PICKAXE -> BlockTags.MINEABLE_WITH_PICKAXE
                    ToolTypes.AXE -> BlockTags.MINEABLE_WITH_AXE
                    ToolTypes.HOE -> BlockTags.MINEABLE_WITH_HOE
                }

                val toolSettings = toolFunction(Item.Properties(), material, damage, speed)
                    .component(DataComponents.LORE, tooltip)

                val registryKey =
                    ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, id))

                val tool =
                    toolClass(material, effectiveBlocks, toolSettings.setId(registryKey))

                Registry.register(
                    BuiltInRegistries.ITEM,
                    ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MODID, id)),
                    tool
                )

                registeredItems.add(tool)

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
        val areaText = Component.literal("${diameter}x${diameter}").withStyle(ChatFormatting.GRAY)

        val shovelLore =
            ItemLore(
                listOf(
                    Component.translatable(
                        "tooltip.additions.radius_mine.shovel_description",
                        areaText
                    ).withStyle(ChatFormatting.WHITE),
                    Component.translatable(
                        "tooltip.additions.radius_mine.path_creation_description"
                    ).withStyle(ChatFormatting.WHITE),
                    Component.translatable(
                        "tooltip.additions.radius_mine.sneak_description"
                    ).withStyle(ChatFormatting.GRAY),
                ),
            )

        val hammerLore =
            ItemLore(
                listOf(
                    Component.translatable(
                        "tooltip.additions.radius_mine.hammer_description",
                        areaText
                    ).withStyle(ChatFormatting.WHITE),
                    Component.translatable(
                        "tooltip.additions.radius_mine.path_creation_description"
                    ).withStyle(ChatFormatting.WHITE),
                    Component.translatable(
                        "tooltip.additions.radius_mine.sneak_description"
                    ).withStyle(ChatFormatting.GRAY),
                ),
            )

        val axeLore =
            ItemLore(
                listOf(
                    Component.translatable(
                        "tooltip.additions.radius_mine.axe_description",
                        areaText
                    ).withStyle(ChatFormatting.WHITE),
                    Component.translatable(
                        "tooltip.additions.radius_mine.stripped_wood_creation_description"
                    ).withStyle(ChatFormatting.WHITE),
                ),
            )

        val pickaxeLore =
            ItemLore(
                listOf(
                    Component.translatable(
                        "tooltip.additions.radius_mine.pickaxe_description",
                        areaText
                    ).withStyle(ChatFormatting.WHITE),
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