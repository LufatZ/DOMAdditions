package de.additions.items


import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.config.AdditionsConfig
import de.additions.items.RadiusMineItem.Companion.materials
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier

object ItemRegistry {
    val registeredItems: MutableList<ItemStack> = mutableListOf()
    private var addedItems: MutableMap<String, Item> = mutableMapOf()
    private val detailedLogging = AdditionsConfig.TranslationLogging || FabricLoader.getInstance().isDevelopmentEnvironment

    fun registerItems() {
        logger.info("Adding items")
        addToolItems()
        logger.info("Registering added items")
        registerAddedItems(addedItems)
        logger.info("Finished item registration with ${registeredItems.size} items")
    }

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

    private fun addToolItems() {
        materials.forEach { (materialName, material) ->
            runCatching {
                val toolSettings = Item.Settings()

                val bigShovel = RadiusMineItem(material, BlockTags.SHOVEL_MINEABLE, 1.5f, -3.0f, toolSettings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, "big_${materialName}_shovel"))))
                val hammer = RadiusMineItem(material, BlockTags.PICKAXE_MINEABLE, 1.0f, -2.8f, toolSettings.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MODID, "${materialName}_hammer"))))

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