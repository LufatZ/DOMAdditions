/**
 * Registry für zusätzliche Minecraft-Blöcke und deren Varianten.
 * Ermöglicht die automatische Registrierung von Treppen und Platten für vorgegebene Basis-Blöcke.
 *
 * @property registeredBlocks Liste aller registrierten Blöcke als ItemStacks
 */
package de.additions.blocks

import de.additions.config.AdditionsConfig
import net.minecraft.block.*
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier

object BlockRegistry {
    /** Speichert alle registrierten Blöcke als ItemStacks für spätere Verwendung */
    val registeredBlocks: MutableList<ItemStack> = mutableListOf()

    /**
     * Erstellt einen Registry-Key für einen Block.
     *
     * @param id Block-Identifier ohne Namespace
     * @return RegistryKey für den Block
     */
    private fun keyOfBlock(id: String): RegistryKey<Block> =
        RegistryKey.of(RegistryKeys.BLOCK, Identifier.of("additions", id))

    /**
     * Erstellt einen Registry-Key für ein Item.
     *
     * @param id Item-Identifier ohne Namespace
     * @return RegistryKey für das Item
     */
    private fun keyOfItem(id: String): RegistryKey<Item> =
        RegistryKey.of(RegistryKeys.ITEM, Identifier.of("additions", id))

    /**
     * Registriert einen Block und sein zugehöriges BlockItem.
     * Fügt außerdem einen ItemStack des Blocks zur registeredBlocks Liste hinzu.
     *
     * @param id Identifier des Blocks ohne Namespace
     * @param block Der zu registrierende Block
     * @return Der registrierte Block
     */
    private fun register(id: String, block: Block): Block {
        val blockKey = keyOfBlock(id)
        val itemKey = keyOfItem(id)

        // Registriere Block und BlockItem
        Registry.register(Registries.BLOCK, blockKey, block)
        val blockItem = BlockItem(block, Item.Settings().registryKey(itemKey))
        Registry.register(Registries.ITEM, itemKey, blockItem)

        // Füge ItemStack zur Liste hinzu
        registeredBlocks.add(ItemStack(blockItem))

        return block
    }

    /**
     * Registriert Treppen- und Platten-Varianten für alle vordefinierten Basis-Blöcke.
     * Die zu registrierenden Blöcke werden durch die AdditionsConfig.EnabledBlockVariants Konfiguration gesteuert.
     */
    fun registerBlocks() {
        // Liste der Basis-Blöcke, für die Varianten erstellt werden sollen
        val predefinedBlocks: List<Block> = if (AdditionsConfig.EnabledBlockVariants) {
            listOf(
                Blocks.DIRT,
                Blocks.PODZOL,
                Blocks.GRASS_BLOCK,
                Blocks.COARSE_DIRT,
                Blocks.MYCELIUM,
                Blocks.ROOTED_DIRT,
                Blocks.MOSS_BLOCK,
                Blocks.MUD,
                Blocks.MUDDY_MANGROVE_ROOTS
            )
        } else {
            listOf()
        }

        // Erstelle Varianten für jeden Basis-Block
        predefinedBlocks.forEach { baseBlock ->
            // Extrahiere Basis-Namen und entferne "_block" Suffix
            val baseName = Registries.BLOCK.getId(baseBlock).path.replace("_block", "")
            // Kopiere Block-Einstellungen vom Basis-Block
            val settings = AbstractBlock.Settings.copy(baseBlock)

            // Registriere Platten-Variante
            register("${baseName}_slab", SlabBlock(
                settings.registryKey(keyOfBlock("${baseName}_slab"))
            ))

            // Registriere Treppen-Variante
            register("${baseName}_stairs", StairsBlock(
                baseBlock.defaultState,
                settings.registryKey(keyOfBlock("${baseName}_stairs"))
            ))
        }
    }
}