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
 * Manages the registration of all custom items for the mod.
 *
 * This object serves as the central registry for all custom items, handling their creation,
 * registration with Minecraft's item registry, and integration into the game's creative tabs.
 * It supports both radius mining tools and vein mining tools across different materials.
 */
object ItemRegistry {
    /**
     * A mutable list of [ItemStack]s for all successfully registered items.
     *
     * This collection is populated during the registration process and serves as a reference
     * for adding items to creative tabs. Each ItemStack represents a registered custom tool
     * that will be available to players in creative mode.
     */
    val registeredItems: MutableList<ItemStack> = mutableListOf()

    /**
     * Initializes the complete item registration process.
     *
     * This is the main entry point for item registration. It orchestrates the creation
     * of all custom tool items, registers them with Minecraft's item registry, and
     * integrates them into the appropriate creative item group. The process includes:
     * 1. Creating tool items with their respective properties
     * 2. Registering items with the game
     * 3. Adding items to the creative tab after the diamond pickaxe
     */
    fun registerItems() {
        logger.info("Adding items")
        addToolItems()
        logger.info("Registering added items")
        logger.info("Finished item registration with ${registeredItems.size} items")
        ItemGroupRegistry.registerItemsAfterCommonParent(registeredItems, Items.DIAMOND_PICKAXE)
    }

    /**
     * Registers tool items for all available materials with specified properties.
     *
     * This function creates and registers tools for each material defined in the materials map.
     * It handles the complete registration process including setting up tool properties,
     * tooltips, effective blocks, and registry keys.
     *
     * @param placeholderId The template ID string where "%material" will be replaced with material names
     * @param tooltip The [LoreComponent] containing the tool's tooltip/description text
     * @param damage The base attack damage value for the tool
     * @param speed The attack speed modifier for the tool (negative values make it slower)
     * @param type The [ToolTypes] enum defining what kind of tool this is (shovel, pickaxe, axe, hoe)
     * @param miningType The [MiningTypes] enum defining the mining behavior (radius or vein mining)
     */
    private fun registerToolItems(placeholderId: String, tooltip: LoreComponent, damage: Float, speed: Float, type: ToolTypes, miningType: MiningTypes) {
        materials.forEach { (materialName, material) ->
            runCatching {
                // Generate the unique item ID by replacing the material placeholder
                val id = placeholderId.replace("%material", materialName)

                // Select the appropriate tool function based on tool type
                val toolFunction = when (type) {
                    ToolTypes.SHOVEL -> Item.Settings::shovel
                    ToolTypes.PICKAXE -> Item.Settings::pickaxe
                    ToolTypes.AXE -> Item.Settings::axe
                    ToolTypes.HOE -> Item.Settings::hoe
                }

                // Select the appropriate tool class based on mining type
                val toolClass = when (miningType) {
                    MiningTypes.RADIUS_MINING -> ::RadiusMineItem
                    MiningTypes.VEIN_MINING -> ::VeinMineItem
                }

                // Determine which blocks this tool can effectively mine
                val effectiveBlocks = when (type) {
                    ToolTypes.SHOVEL -> BlockTags.SHOVEL_MINEABLE
                    ToolTypes.PICKAXE -> BlockTags.PICKAXE_MINEABLE
                    ToolTypes.AXE -> BlockTags.AXE_MINEABLE
                    ToolTypes.HOE -> BlockTags.HOE_MINEABLE
                }

                // Configure the tool settings with material properties, damage, speed, and tooltip
                val toolSettings = toolFunction(Item.Settings(), material, damage, speed)
                    .component(DataComponentTypes.LORE, tooltip)

                // Create the registry key for this specific tool
                val registryKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, id))

                // Instantiate the tool with all configured properties
                val tool = toolClass(material, effectiveBlocks, toolSettings.registryKey(registryKey))

                // Register the tool with Minecraft's item registry
                Registry.register(Registries.ITEM, RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, id)), tool)

                // Add the tool to our list of registered items for creative tab integration
                registeredItems.add(ItemStack(tool))

                // Log successful registration if logging is enabled
                if (logging) {
                    logger.info("registered tool item: $id")
                }
            }.onFailure { e ->
                // Log any registration failures with material context and full stack trace
                logger.error("Failed to register tool items for material: $materialName")
                logger.error(e.stackTraceToString())
            }
        }
    }

    /**
     * Creates and registers all custom tool items with their specific properties.
     *
     * This function defines and creates three types of custom tools:
     * 1. Big Shovels - Radius mining shovels that mine in a 5x5 area and can create paths
     * 2. Hammers - Radius mining pickaxes that mine in a 5x5 area and can create paths
     * 3. Big Axes - Vein mining axes that can strip wood and mine connected blocks
     *
     * Each tool type is created for all available materials with appropriate damage,
     * speed, and tooltip configurations. The tooltips include localized descriptions
     * of the tool's special abilities and usage instructions.
     */
    private fun addToolItems() {
        // Calculate the mining area dimensions based on the radius constant
        val diameter = 2 * RADIUS + 1
        val areaText = Text.literal("${diameter}x${diameter}").formatted(Formatting.GRAY)

        // Create tooltip for big shovels with area mining and path creation abilities
        val shovelLore =
            LoreComponent(
                listOf(
                    Text.translatable("tooltip.additions.radius_mine.shovel_description", areaText)
                        .formatted(Formatting.WHITE),
                    Text.translatable("tooltip.additions.radius_mine.path_creation_description")
                        .formatted(Formatting.WHITE),
                    Text.translatable("tooltip.additions.radius_mine.sneak_description").formatted(Formatting.GRAY),
                ),
            )

        // Create tooltip for hammers with area mining and path creation abilities
        val hammerLore =
            LoreComponent(
                listOf(
                    Text.translatable("tooltip.additions.radius_mine.hammer_description", areaText)
                        .formatted(Formatting.WHITE),
                    Text.translatable("tooltip.additions.radius_mine.path_creation_description")
                        .formatted(Formatting.WHITE),
                    Text.translatable("tooltip.additions.radius_mine.sneak_description").formatted(Formatting.GRAY),
                ),
            )

        // Create tooltip for big axes with vein mining and wood stripping abilities
        val axeLore =
            LoreComponent(
                listOf(
                    Text.translatable("tooltip.additions.radius_mine.axe_description", areaText)
                        .formatted(Formatting.WHITE),
                    Text.translatable("tooltip.additions.radius_mine.stripped_wood_creation_description")
                        .formatted(Formatting.WHITE),
                ),
            )

        // Register big shovels: moderate damage (1.5), slow speed (-3.0), radius mining
        registerToolItems("big_%material_shovel", shovelLore, 1.5f, -3.0f, ToolTypes.SHOVEL, MiningTypes.RADIUS_MINING)

        // Register hammers: lower damage (1.0), slightly faster than shovels (-2.8), radius mining
        registerToolItems("%material_hammer", hammerLore, 1f, -2.8f, ToolTypes.PICKAXE, MiningTypes.RADIUS_MINING)

        // Register big axes: high damage (6.0), slowest speed (-3.2), vein mining for tree cutting
        registerToolItems("big_%material_axe", axeLore, 6f, -3.2f, ToolTypes.AXE, MiningTypes.VEIN_MINING)
    }
}