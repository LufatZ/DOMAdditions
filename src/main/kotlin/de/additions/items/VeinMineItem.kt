@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.items

import net.minecraft.block.Block
import net.minecraft.item.ToolMaterial
import net.minecraft.registry.tag.TagKey


class VeinMineItem(
    material: ToolMaterial,
    effectiveBlocks: TagKey<Block>,
    settings: Settings,
) : ToolItem(material, effectiveBlocks, settings) {

}
