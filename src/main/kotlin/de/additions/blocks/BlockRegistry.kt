/**
 * Registry für zusätzliche Minecraft-Blöcke und deren Varianten.
 * Ermöglicht die automatische Registrierung von Treppen, Platten und Laternen-Varianten.
 *
 * @property registeredBlocks Liste aller registrierten Blöcke als ItemStacks
 */
package de.additions.blocks

import de.additions.Additions.MODID
import de.additions.Additions.logger
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

    /** Zentraler Registrierungsaufruf für alle Blocktypen */
    fun registerAllBlocks() {
        logger.info("Initiating block registration process")
        registerBlockVariants()
        registerLanternVariants()
        logger.info("Block registration completed. Total registered blocks: ${registeredBlocks.size}")
    }

    /**
     * Erstellt einen Registry-Key mit flexiblen Optionen.
     * Unterstützt Vanilla- und Mod-spezifische Ressourcen.
     *
     * @param id Identifier des Ressourcen-Elements
     * @param vanilla Gibt an, ob es sich um ein Vanilla-Element handelt
     * @param type Der Typ der Registry (Block, Item, etc.)
     * @return Der erstellte RegistryKey
     */
    private inline fun <reified T> keyOf(
        id: String,
        vanilla: Boolean = false,
        type: RegistryKey<Registry<T>> = RegistryKeys.BLOCK as RegistryKey<Registry<T>>
    ): RegistryKey<T> {
        val identifier = if (vanilla) {
            logger.debug("Creating Vanilla registry key for: $id")
            Identifier.ofVanilla(id)
        } else {
            logger.debug("Creating Mod-specific registry key for: $id")
            Identifier.of(MODID, id)
        }

        return RegistryKey.of(type, identifier)
    }

    /**
     * Registriert einen Block und sein zugehöriges BlockItem.
     *
     * @param id Identifier des Blocks ohne Namespace
     * @param block Der zu registrierende Block
     * @return Der registrierte Block
     */
    private fun register(id: String, block: Block): Block {
        logger.debug("Registering block variant: $id")

        val blockKey = keyOf(id = id, type = RegistryKeys.BLOCK)
        val itemKey = keyOf(id = id, type = RegistryKeys.ITEM)

        // Registriere Block und BlockItem
        Registry.register(Registries.BLOCK, blockKey, block)
        val blockItem = BlockItem(block, Item.Settings().registryKey(itemKey))
        Registry.register(Registries.ITEM, itemKey, blockItem)

        // Füge ItemStack zur Liste hinzu
        registeredBlocks.add(ItemStack(blockItem))

        logger.debug("Successfully registered block and block item: $id")
        return block
    }

    /**
     * Registriert Treppen- und Platten-Varianten für vorgegebene Basis-Blöcke.
     */
    fun registerBlockVariants() {
        logger.info("Starting block variant registration")

        // Liste der Basis-Blöcke für Varianten
        val blockVariants: List<Block> = if (AdditionsConfig.EnabledBlockVariants)
            listOf(
                Blocks.DIRT, Blocks.DIRT_PATH, Blocks.PODZOL, Blocks.GRASS_BLOCK,
                Blocks.COARSE_DIRT, Blocks.MYCELIUM, Blocks.ROOTED_DIRT,
                Blocks.MOSS_BLOCK, Blocks.MUD, Blocks.MUDDY_MANGROVE_ROOTS,
                Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK, Blocks.DIAMOND_BLOCK,
                Blocks.SMOOTH_BASALT, Blocks.POLISHED_BASALT,
                Blocks.MAGMA_BLOCK, Blocks.OBSIDIAN, Blocks.CRYING_OBSIDIAN
            )
        else {
            logger.info("Block Variants disabled in configuration")
            listOf()
        }

        // Varianten für jeden Basis-Block erstellen
        blockVariants.forEach { baseBlock ->
            val baseName = Registries.BLOCK.getId(baseBlock).path.replace("_block", "")
            val settings = AbstractBlock.Settings.copy(baseBlock)

            logger.debug("Creating variants for base block: $baseName")

            // Platten-Variante registrieren
            register("${baseName}_slab", SlabBlock(
                settings.registryKey(keyOf("${baseName}_slab"))
            ))

            // Treppen-Variante registrieren
            register("${baseName}_stairs", StairsBlock(
                baseBlock.defaultState,
                settings.registryKey(keyOf("${baseName}_stairs"))
            ))
        }

        logger.info("Block variant registration completed")
    }

    /**
     * Registriert Laternen- und Ketten-Varianten für vorgegebene Basis-Blöcke.
     */
    fun registerLanternVariants() {
        logger.info("Starting lantern variant registration")

        val lanternVariants: List<Block> = if (AdditionsConfig.EnabledLantern)
            listOf(
                Blocks.NETHERITE_BLOCK,
                Blocks.COPPER_BLOCK,
                Blocks.DIAMOND_BLOCK
            )
        else {
            logger.info("Lantern Variants disabled in configuration")
            listOf()
        }

        lanternVariants.forEach { baseBlock ->
            val baseName = Registries.BLOCK.getId(baseBlock).path.replace("_block", "")
            val lanternSettings = AbstractBlock.Settings.copy(Blocks.LANTERN)
            val chainSettings = AbstractBlock.Settings.copy(Blocks.CHAIN)

            logger.debug("Creating lantern and chain variants for base block: $baseName")

            // Laternen-Variante registrieren
            register("${baseName}_lantern", LanternBlock(
                lanternSettings.registryKey(keyOf("${baseName}_lantern"))
            ))

            // Ketten-Variante registrieren
            register("${baseName}_chain", ChainBlock(
                chainSettings.registryKey(keyOf("${baseName}_chain"))
            ))

            // Redstone-Varianten Logging
            if (AdditionsConfig.EnabledRedstoneLantern) {
                logger.warn("Redstone Lanterns enabled but not yet implemented for $baseName")
            } else {
                logger.debug("Redstone Lanterns disabled for $baseName")
            }
        }

        logger.info("Lantern variant registration completed")
    }
}