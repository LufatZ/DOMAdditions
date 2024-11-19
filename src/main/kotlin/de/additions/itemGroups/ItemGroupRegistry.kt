package de.additions.itemGroups

import de.additions.Additions.MODID
import de.additions.blocks.BlockRegistry
import de.additions.items.ItemRegistry
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.block.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.text.Text
import net.minecraft.util.Identifier

object ItemGroupRegistry {

    fun registerItemGroup(name: String, icon: ItemStack, items: List<ItemStack>) {
        val groupKey = RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(MODID, name))

        Registry.register(Registries.ITEM_GROUP, groupKey, FabricItemGroup.builder()
            .displayName(Text.translatable("itemGroup.$MODID.$name"))
            .icon { icon }
            .entries { _, entries ->
                entries.addAll(items)
            }
            .build()
        )
    }

    //register Item Groups here
    fun registerItemGroups(){
        registerItemGroup(  //all Blocks from DayOfMind
            name = "blocks",
            icon = ItemStack(Blocks.DIRT),
            items = BlockRegistry.registeredBlocks.toList()
        )

        registerItemGroup(  //all items from DayOfMind
            name = "items",
            icon = ItemStack(Items.DIAMOND_PICKAXE),
            items = ItemRegistry.registeredItems.toList()
        )
    }
}