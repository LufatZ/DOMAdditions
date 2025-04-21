package de.additions.itemGroups

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry
import de.additions.config.AdditionsConfig
import de.additions.items.ItemRegistry
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.item.ItemGroups
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier

/**
 * ItemGroupRegistry manages the creation and registration of custom item groups (creative tabs)
 * and provides methods for adding items to default Minecraft item groups.
 *
 * This object handles the organization of mod items and blocks within the Minecraft inventory interface.
 */
object ItemGroupRegistry {
    /**
     * Registers a custom item group (creative tab) with a specified name, icon, and initial items.
     *
     * @param name The internal name of the item group, used for translation and identification
     * @param icon The ItemStack to be used as the group's icon
     * @param items A list of ItemStacks to be initially added to the group
     * @return The registered RegistryKey for the created item group
     */
    fun registerItemGroup(
        name: String,
        icon: ItemStack,
        items: List<ItemStack>,
    ) {
        val groupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(MODID, name))

        Registry.register(
            Registries.ITEM_GROUP,
            groupKey,
            FabricItemGroup
                .builder()
                .displayName(Text.translatable("itemGroup.$MODID.$name"))
                .icon { icon }
                .entries { _, entries ->
                    entries.addAll(items)
                }.build(),
        )
    }

    /**
     * Initializes and registers the mod's custom item groups.
     *
     * This method should be called during mod initialization to set up all custom creative tabs.
     * Currently creates two groups:
     * - A group for all blocks from the mod
     * - A group for all items from the mod
     */
    fun registerItemGroups() {
        registerItemGroup( // all Blocks from DayOfMind
            name = "blocks",
            icon = ItemStack(BlockRegistry.registeredBlocks[0].item),
            items = BlockRegistry.registeredBlocks.toList(),
        )

        registerItemGroup( // all items from DayOfMind
            name = "items",
            icon = ItemStack(ItemRegistry.registeredItems[0].item),
            items = ItemRegistry.registeredItems.toList(),
        )
    }

    /**
     * Adds mod blocks to default Minecraft item groups.
     *
     * This method registers event listeners to modify entries in specific default item groups:
     * - Building Blocks
     * - Natural Blocks
     *
     * @param items List of mod blocks to be added to default groups
     * @param parentItems List of vanilla/existing blocks that the mod blocks should be placed after
     */
    fun registerItemsInDefaultGroups(
        items: List<Item>,
        parentItems: List<Item>,
    ) {
        // Add to Building Blocks group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        // Add to Natural Blocks group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.NATURAL).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        // Add to Redstone group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        // Add to Tools group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        // Add to Combat group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        // Add to Colored Blocks group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COLORED_BLOCKS).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        // Add to Food and drink group
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.SPAWN_EGGS).register { itemGroup ->
            addToGroupAfterParent(itemGroup, items, parentItems)
        }
    }

    fun registerBlocksInDefaultGroups(
        blocks: List<Block>,
        parentBlocks: List<Block>,
    ) {
        val items = mutableListOf<Item>()
        val parentItems = mutableListOf<Item>()
        blocks.forEach { items.add(it.asItem()) }
        parentBlocks.forEach { parentItems.add(it.asItem()) }
        registerItemsInDefaultGroups(items, parentItems)
    }

    /**
     * Registers mod items in default Minecraft item groups using a single, common parent block.
     *
     * This method simplifies the process of adding multiple related items to various default
     * Minecraft creative tabs when these items share a common parent block. It creates an internal
     * list of parent blocks, where each item is associated with the same parent.
     *
     * Useful scenarios:
     * - Adding multiple variant blocks derived from a single base block
     * - Registering related items that logically stem from one core block type
     * - Streamlining item group integration for mods with consistent block families
     *
     * @param items A list of blocks to be added to default Minecraft creative tabs
     * @param parentItem The common parent block that serves as a reference point for item placement
     *
     * @sample
     * // Example: Adding stone variant blocks after the base stone block
     * addItemsAfterSingleBlock(
     *     items = listOf(polishedStoneBlock, smoothStoneBlock, graniteSlab),
     *     parentItem = Blocks.STONE
     * )
     */
    fun registerItemsAfterCommonParent(
        items: List<Block>,
        parentItem: Block,
    ) {
        val parentList = mutableListOf<Block>()
        items.forEach { _ -> parentList.add(parentItem) }
        registerBlocksInDefaultGroups(items, parentList)
    }

    fun registerItemsAfterCommonParent(
        stacks: List<ItemStack>,
        parentItem: Item,
    ) {
        val parentList = mutableListOf<Item>()
        val items = mutableListOf<Item>()
        stacks.forEach { items.add(it.item) }
        items.forEach { _ -> parentList.add(parentItem) }
        registerItemsInDefaultGroups(items, parentList)
    }

    /**
     * Helper method to add items to a specific group after a parent item.
     *
     * This method iterates through the provided items and parent items, and:
     * - Checks if the parent item exists in the current group
     * - Adds the new item directly after the parent item if found
     * - Logs the addition for debugging purposes
     *
     * @param itemGroup The FabricItemGroupEntries to modify
     * @param items List of blocks to be added
     * @param parents List of parent blocks to place the new items after
     */
    private fun addToGroupAfterParent(
        itemGroup: FabricItemGroupEntries,
        items: List<Item>,
        parents: List<Item>,
    ) {
        parents.forEachIndexed { index, parent ->
            val item = items[index]
            val parentItem = parent

            // Check if parent item exists in the current group
            if (itemGroup.displayStacks.any { it.item == parentItem }) {
                // Add the new item right after the parent item
                itemGroup.addAfter(parentItem, item)
                if (AdditionsConfig.DetailedLogging || FabricLoader.getInstance().isDevelopmentEnvironment) {
                    logger.info("Added $item to $itemGroup after $parentItem")
                }
            }
        }
    }
}
