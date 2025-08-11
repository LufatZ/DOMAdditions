package de.additions.items

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.config.AdditionsConfig
import de.additions.itemGroups.ItemGroupRegistry
import de.additions.items.RadiusMineItem.Companion.RADIUS
import de.additions.items.RadiusMineItem.Companion.materials
import net.fabricmc.loader.api.FabricLoader
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
 * This object is responsible for creating, registering, and tracking all new items,
 * ensuring they are correctly added to the game and creative item groups.
 */
object ItemRegistry {
    /**
     * A list of [ItemStack]s for all successfully registered items.
     * This list is populated during registration and used to add the items to the appropriate creative tab.
     */
    val registeredItems: MutableList<ItemStack> = mutableListOf()

    /**
     * A temporary map holding items that have been created but not yet registered.
     * The key is the item's intended name (e.g., "big_diamond_shovel"), and the value is the [Item] instance.
     */
    private var addedItems: MutableMap<String, Item> = mutableMapOf()
    private val detailedLogging = AdditionsConfig.DetailedLogging || FabricLoader.getInstance().isDevelopmentEnvironment

    /**
     * Initializes the item registration process.
     * This function orchestrates the creation of tool items, registers them with the game,
     * and then adds them to the creative item group.
     */
    fun registerItems() {
        logger.info("Adding items")
        addToolItems()
        logger.info("Registering added items")
        registerAddedItems(addedItems)
        logger.info("Finished item registration with ${registeredItems.size} items")
        ItemGroupRegistry.registerItemsAfterCommonParent(registeredItems, Items.DIAMOND_PICKAXE)
    }

    /**
     * Registers the items held in the [addedItems] map.
     * It iterates through the map, registers each item with Minecraft's item registry,
     * and adds a corresponding [ItemStack] to the [registeredItems] list.
     *
     * @param items A map of item names to [Item] instances to be registered.
     */
    private fun registerAddedItems(items: Map<String, Item>) {
        items.forEach { (itemName, item) ->
            runCatching {
                Registry.register(Registries.ITEM, RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, itemName)), item)
                registeredItems.add(ItemStack(item))
            }.onSuccess {
                if (detailedLogging) {
                    logger.info("Registered added item: $itemName")
                }
            }.onFailure { e ->
                logger.error("Failed to register added item: $itemName")
                logger.error(e.stackTraceToString())
            }
        }
    }

    /**
     * Creates all tool items based on the defined materials.
     * This function iterates through the [materials] list from [RadiusMineItem] and creates
     * a "big shovel" and a "hammer" for each material, adding them to the [addedItems] map.
     */
    private fun addToolItems() {
        materials.forEach { (materialName, material) ->
            runCatching {
                val diameter = 2 * RADIUS + 1
                val areaText = Text.literal("${diameter}x${diameter}").formatted(Formatting.GRAY)

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

                val shovelSettings =
                    Item.Settings().shovel(material, 1.5f, -3.0f).component(DataComponentTypes.LORE, shovelLore)
                val hammerSettings =
                    Item.Settings().pickaxe(material, 1f, -2.8f).component(DataComponentTypes.LORE, hammerLore)

                val bigShovel =
                    RadiusMineItem(
                        material,
                        BlockTags.SHOVEL_MINEABLE,
                        shovelSettings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, "big_${materialName}_shovel"))),
                    )
                val hammer =
                    RadiusMineItem(
                        material,
                        BlockTags.PICKAXE_MINEABLE,
                        hammerSettings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, "${materialName}_hammer"))),
                    )

                addedItems["big_${materialName}_shovel"] = bigShovel
                addedItems["${materialName}_hammer"] = hammer

                if (detailedLogging) {
                    logger.info("Added big shovel and hammer for material: $materialName - not registered yet")
                }
            }.onFailure { e ->
                logger.error("Failed to create tool items for material: $materialName")
                logger.error(e.stackTraceToString())
            }
        }
    }
}
