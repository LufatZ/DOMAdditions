package de.additions.items

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.config.AdditionsConfig
import de.additions.itemGroups.ItemGroupRegistry
import de.additions.items.RadiusMineItem.Companion.materials
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier

/**
 * Registers custom items for the mod (Original 1.21.4 Version).
 */
object ItemRegistry {
    val registeredItems: MutableList<ItemStack> = mutableListOf()
    private var addedItems: MutableMap<String, Item> = mutableMapOf()
    private val detailedLogging = AdditionsConfig.DetailedLogging || FabricLoader.getInstance().isDevelopmentEnvironment

    /**
     * Registers all custom items defined in this registry.
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
     * Registers items present in the provided map into the game's item registry.
     * @param items A map where keys are item names (used for ID) and values are Item instances.
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
     * Creates instances of RadiusMineItem tools (Hammers and Big Shovels) for each defined material
     * and adds them to the `addedItems` map for later registration. Uses 1.21.4 item instantiation.
     */
    private fun addToolItems() {
        materials.forEach { (materialName, material) ->
            runCatching {
                // Use basic Item.Settings() - durability etc. handled by MiningToolItem super constructor via applyToolSettings
                val toolSettings = Item.Settings()
                // Note: Add .group(ItemGroup.TOOLS) or similar here if needed in 1.21.4

                // Instantiate RadiusMineItem correctly for 1.21.4, passing AD/AS explicitly
                val bigShovel =
                    RadiusMineItem(
                        material,
                        BlockTags.SHOVEL_MINEABLE,
                        1.5f, // Attack Damage
                        -3.0f, // Attack Speed
                        // Apply registry key if needed by settings setup in 1.21.4
                        toolSettings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, "big_${materialName}_shovel"))),
                    )
                val hammer =
                    RadiusMineItem(
                        material,
                        BlockTags.PICKAXE_MINEABLE,
                        1.0f, // Attack Damage
                        -2.8f, // Attack Speed
                        // Apply registry key if needed by settings setup in 1.21.4
                        toolSettings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, "${materialName}_hammer"))),
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
