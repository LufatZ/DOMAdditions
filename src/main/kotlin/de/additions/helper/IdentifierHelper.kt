package de.additions.helper

import net.minecraft.world.level.block.Block
import net.minecraft.world.item.Item
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier

object IdentifierHelper {
    /**
     * Helper functions for retrieving [Identifier]s from various Minecraft objects.
     */
    fun getId(item: Item): Identifier = BuiltInRegistries.ITEM.getKey(item)
    fun getId(block: Block): Identifier = BuiltInRegistries.BLOCK.getKey(block)
}