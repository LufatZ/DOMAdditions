/**
 * Registry für zusätzliche Minecraft-Blöcke und deren Varianten.
 * Ermöglicht die automatische Registrierung von Treppen und Platten für vorgegebene Basis-Blöcke.
 *
 * @property registeredBlocks Liste aller registrierten Blöcke als ItemStacks
 */
package de.additions.blocks

import de.additions.Additions.MODID
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
    /** Registrierungsaufruf für alle Blöcke */
    fun registerAllBlocks() {
        registerBlockVariants()
    }
    /** Speichert alle registrierten Blöcke als ItemStacks für spätere Verwendung */
    val registeredBlocks: MutableList<ItemStack> = mutableListOf()

    /**
     * Erstellt einen Registry-Key mit flexiblen Optionen.
     *
     * @param id Identifier des Ressourcen-Elements
     * @param vanilla Gibt an, ob es sich um ein Vanilla-Element handelt
     * @param type Der Typ der Registry (Block, Item, etc.)
     * @return Der erstellte RegistryKey
     */
    private inline fun <reified T> keyOf(id: String, vanilla: Boolean = true, type: RegistryKey<Registry<T>> = RegistryKeys.BLOCK as RegistryKey<Registry<T>>): RegistryKey<T> {
        val identifier = if (vanilla) {
            Identifier.ofVanilla(id)
        } else {
            Identifier.of(MODID, id)
        }

        return RegistryKey.of(type, identifier)
    }

    /**
     * Registriert einen Block und sein zugehöriges BlockItem.
     * Fügt außerdem einen ItemStack des Blocks zur registeredBlocks Liste hinzu.
     *
     * @param id Identifier des Blocks ohne Namespace
     * @param block Der zu registrierende Block
     * @return Der registrierte Block
     */
    private fun register(id: String, block: Block): Block {
        val blockKey = keyOf(id= id, type = RegistryKeys.BLOCK)
        val itemKey = keyOf(id= id, type = RegistryKeys.ITEM)

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
    fun registerBlockVariants() {
        // Liste der Basis-Blöcke, für die Varianten erstellt werden sollen
        val blockVariants: List<Block> = if (AdditionsConfig.EnabledBlockVariants) {
            listOf(
                Blocks.DIRT,
                Blocks.DIRT_PATH,
                Blocks.PODZOL,
                Blocks.GRASS_BLOCK,
                Blocks.COARSE_DIRT,
                Blocks.MYCELIUM,
                Blocks.ROOTED_DIRT,
                Blocks.MOSS_BLOCK,
                Blocks.MUD,
                Blocks.MUDDY_MANGROVE_ROOTS,
                Blocks.GOLD_BLOCK,
                Blocks.IRON_BLOCK,
                Blocks.DIAMOND_BLOCK,
                Blocks.SMOOTH_BASALT,
                Blocks.POLISHED_BASALT,
                Blocks.MAGMA_BLOCK,
                Blocks.OBSIDIAN,
                Blocks.CRYING_OBSIDIAN
            )
        } else {
            listOf()
        }

        // Erstelle Varianten für jeden Basis-Block
        blockVariants.forEach { baseBlock ->
            // Extrahiere Basis-Namen und entferne "_block" Suffix
            val baseName = Registries.BLOCK.getId(baseBlock).path.replace("_block", "")
            // Kopiere Block-Einstellungen vom Basis-Block
            val settings = AbstractBlock.Settings.copy(baseBlock)

            // Registriere Platten-Variante
            register("${baseName}_slab", SlabBlock(
                settings.registryKey(keyOf("${baseName}_slab"))
            ))

            // Registriere Treppen-Variante
            register("${baseName}_stairs", StairsBlock(
                baseBlock.defaultState,
                settings.registryKey(keyOf("${baseName}_stairs"))
            ))
        }
    }
}