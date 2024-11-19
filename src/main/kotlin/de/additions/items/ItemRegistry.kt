package de.additions.items


import net.minecraft.item.ItemStack
import net.minecraft.item.Items

object ItemRegistry {
    val registeredItems: MutableList<ItemStack> = mutableListOf()

    fun registerItems() {
        registeredItems.add(ItemStack(Items.ITEM_FRAME))
    }
}