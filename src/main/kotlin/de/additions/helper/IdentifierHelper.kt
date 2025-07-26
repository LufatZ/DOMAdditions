package de.additions.helper

import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

object IdentifierHelper {
    /**
     * Helper functions for retrieving [Identifier]s from various Minecraft objects.
     */
    fun getId(item: Item): Identifier = Registries.ITEM.getId(item)
    fun getId(block: Block): Identifier = Registries.BLOCK.getId(block)
}