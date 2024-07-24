package de.additions.blocks

import de.additions.Additions
import de.additions.config.AdditionsConfig
import net.minecraft.block.*
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object AdditionsBlocks {
    private fun register(block: Block, name: String, shouldRegisterItem: Boolean = true): Block {
        val id = Identifier.of(Additions.MODID, name)

        if (shouldRegisterItem) {
            val blockItem = BlockItem(block, Item.Settings())
            Registry.register(Registries.ITEM, id, blockItem)
        }

        return Registry.register(Registries.BLOCK, id, block)
    }

    fun registerBlocks() {
        var predefinedBlocks: List<Block> = listOf()
            if (AdditionsConfig.EnabledBlockVariants) {
                predefinedBlocks = listOf(
                Blocks.DIRT,
                Blocks.PODZOL,
                Blocks.GRASS_BLOCK,
                Blocks.COARSE_DIRT,
                Blocks.MYCELIUM,
                Blocks.ROOTED_DIRT,
                Blocks.MOSS_BLOCK,
                Blocks.MUD,
                Blocks.MUDDY_MANGROVE_ROOTS
                    // Weitere Blöcke hier hinzufügen
                )
            }

        predefinedBlocks.forEach { baseBlock ->
            val baseName = Registries.BLOCK.getId(baseBlock).path.replace("_block","")
            register(
                SlabBlock(AbstractBlock.Settings.copy(baseBlock)),
                "${baseName}_slab"
            )

            register(
                StairsBlock(baseBlock.defaultState, AbstractBlock.Settings.copy(baseBlock)),
                "${baseName}_stairs"
            )
        }
    }
}
