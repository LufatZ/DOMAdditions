package de.additions.itemGroups

import de.additions.Additions.MODID
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

object ItemGroups {

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

    fun registerItemGroups(){
        // Registrieren Sie eine neue Item-Gruppe
        registerItemGroup(
            name = "blocks",
            icon = ItemStack(Blocks.DIRT),
            items = listOf(
                ItemStack(Blocks.DIRT),
                ItemStack(Blocks.STONE),
                ItemStack(Blocks.GRASS_BLOCK)
            )
        )

        // Registrieren Sie eine weitere Item-Gruppe
        registerItemGroup(
            name = "items",
            icon = ItemStack(Items.DIAMOND_PICKAXE),
            items = listOf(
                ItemStack(Items.WOODEN_PICKAXE),
                ItemStack(Items.STONE_PICKAXE),
                ItemStack(Items.IRON_PICKAXE)
            )
        )
    }
}