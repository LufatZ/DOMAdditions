/**
 * Registry für zusätzliche Minecraft-Blöcke und deren Varianten.
 * Ermöglicht die automatische Registrierung von Treppen, Platten und Laternen-Varianten.
 */
@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.blocks

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.config.AdditionsConfig
import de.additions.itemGroups.ItemGroupRegistry
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

    @JvmStatic
    val registeredStairs: MutableList<Block> = mutableListOf()

    @JvmStatic
    val registeredSlabs: MutableList<Block> = mutableListOf()
    val registeredLanterns: MutableMap<Block, Block> = mutableMapOf()
    val registeredTrapdoors: MutableList<Block> = mutableListOf()
    val registeredChains: MutableList<Block> = mutableListOf()
    val registeredGrassBlocks: MutableList<Block> = mutableListOf()
    private val registeredDirtBlockVariants: MutableList<Block> = mutableListOf()
    private val registeredMagmaBlockVariants: MutableList<Block> = mutableListOf()

    @Suppress("ktlint:standard:property-naming")
    var DIRT_PATH_STAIRS: Block = Blocks.OAK_STAIRS // Fallback-Block
        private set

    @Suppress("ktlint:standard:property-naming")
    var DIRT_PATH_SLAB: Block = Blocks.OAK_SLAB // Fallback-Block
        private set

    @Suppress("ktlint:standard:property-naming")
    var DIRT_STAIRS: Block = Blocks.OAK_STAIRS // Fallback-Block
        private set

    @Suppress("ktlint:standard:property-naming")
    var DIRT_SLAB: Block = Blocks.OAK_SLAB // Fallback-Block
        private set

    // Listen der Basis-Blöcke für Varianten
    @JvmStatic
    val blockVariantsParents: List<Block> =
        if (AdditionsConfig.EnabledBlockVariants) {
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
                Blocks.CRYING_OBSIDIAN,
            )
        } else {
            logger.info("Block Variants disabled in configuration")
            listOf()
        }
    private val lanternVariantsParents: List<Block> =
        if (AdditionsConfig.EnabledLantern) {
            listOf(
                Blocks.NETHERITE_BLOCK,
                Blocks.COPPER_BLOCK,
                Blocks.DIAMOND_BLOCK,
                Blocks.EMERALD_BLOCK,
                Blocks.GOLD_BLOCK,
                Blocks.IRON_BLOCK,
                Blocks.AMETHYST_BLOCK,
            )
        } else {
            logger.info("Lantern Variants disabled in configuration")
            listOf()
        }
    val trapdoorVariantsParents: List<Block> =
        if (AdditionsConfig.EnabledTrapdoor) {
            listOf(
                Blocks.NETHERITE_BLOCK,
                Blocks.DIAMOND_BLOCK,
                Blocks.STONE,
                Blocks.COBBLESTONE,
                Blocks.MOSSY_COBBLESTONE,
                Blocks.SMOOTH_STONE,
                Blocks.STONE_BRICKS,
                Blocks.CHISELED_STONE_BRICKS,
                Blocks.MOSSY_STONE_BRICKS,
                Blocks.GRANITE,
                Blocks.POLISHED_GRANITE,
                Blocks.DIORITE,
                Blocks.POLISHED_DIORITE,
                Blocks.ANDESITE,
                Blocks.POLISHED_ANDESITE,
                Blocks.DEEPSLATE,
                Blocks.COBBLED_DEEPSLATE,
                Blocks.CHISELED_DEEPSLATE,
                Blocks.POLISHED_DEEPSLATE,
                Blocks.DEEPSLATE_BRICKS,
                Blocks.DEEPSLATE_TILES,
                Blocks.TUFF,
                Blocks.CHISELED_TUFF,
                Blocks.POLISHED_TUFF,
                Blocks.TUFF_BRICKS,
                Blocks.CHISELED_TUFF_BRICKS,
                Blocks.BRICKS,
                Blocks.MUD_BRICKS,
                Blocks.SANDSTONE,
                Blocks.CHISELED_SANDSTONE,
                Blocks.SMOOTH_SANDSTONE,
                Blocks.RED_SANDSTONE,
                Blocks.CHISELED_RED_SANDSTONE,
                Blocks.SMOOTH_RED_SANDSTONE,
                Blocks.PRISMARINE,
                Blocks.PRISMARINE_BRICKS,
                Blocks.DARK_PRISMARINE,
                Blocks.NETHERRACK,
                Blocks.NETHER_BRICKS,
                Blocks.RED_NETHER_BRICKS,
                Blocks.BASALT,
                Blocks.SMOOTH_BASALT,
                Blocks.POLISHED_BASALT,
                Blocks.BLACKSTONE,
                Blocks.CHISELED_POLISHED_BLACKSTONE,
                Blocks.POLISHED_BLACKSTONE,
                Blocks.POLISHED_BLACKSTONE_BRICKS,
                Blocks.END_STONE,
                Blocks.END_STONE_BRICKS,
                Blocks.PURPUR_BLOCK,
                Blocks.GOLD_BLOCK,
                Blocks.EMERALD_BLOCK,
                Blocks.QUARTZ_BLOCK,
                Blocks.CHISELED_QUARTZ_BLOCK,
                Blocks.QUARTZ_BRICKS,
                Blocks.SMOOTH_QUARTZ,
                Blocks.AMETHYST_BLOCK,
            )
        } else {
            logger.info("Trapdoor Variants disabled in configuration")
            listOf()
        }

    /** Get registered Magma Block variants for BubbleColumnBlockMixin */
    @JvmStatic
    fun getRegisteredMagmaBlocks() = registeredMagmaBlockVariants.toList()

    /** Zentraler Registrierungsaufruf für alle Blocktypen */
    fun registerAllBlocks() {
        logger.info("Initiating block registration process")
        registerBlockVariants()
        registerLanternVariants()
        registerTrapdoorVariants()
        registerChains()
        logger.info("Block registration completed. Total registered blocks: ${registeredBlocks.size}")

        // Füge alle registrierten Blöcke zu den Standard-Item-Gruppen hinzu
        ItemGroupRegistry.registerItemsAfterCommonParent(registeredLanterns.keys.toList(), Blocks.LANTERN)
        ItemGroupRegistry.registerItemsAfterCommonParent(registeredChains, Blocks.CHAIN)
        ItemGroupRegistry.registerBlocksInDefaultGroups(registeredTrapdoors, trapdoorVariantsParents)
        ItemGroupRegistry.registerBlocksInDefaultGroups(registeredSlabs, blockVariantsParents)
        ItemGroupRegistry.registerBlocksInDefaultGroups(registeredStairs, blockVariantsParents)
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
        vanilla: Boolean = true,
        type: RegistryKey<Registry<T>>? = null,
    ): RegistryKey<T> {
        @Suppress("UNCHECKED_CAST")
        val resolvedType =
            type ?: when (T::class) {
                Block::class -> RegistryKeys.BLOCK as RegistryKey<Registry<T>>
                Item::class -> RegistryKeys.ITEM as RegistryKey<Registry<T>>
                else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
            }

        val identifier =
            if (vanilla) {
                logger.debug("Creating Vanilla registry key for: $id")
                Identifier.ofVanilla(id)
            } else {
                logger.debug("Creating Mod-specific registry key for: $id")
                Identifier.of(MODID, id)
            }

        return RegistryKey.of(resolvedType, identifier)
    }

    /**
     * Registriert einen Block und sein zugehöriges BlockItem.
     *
     * @param id Identifier des Blocks ohne Namespace
     * @param block Der zu registrierende Block
     * @return Der registrierte Block
     */
    private fun register(
        id: String,
        block: Block,
    ): Block {
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
    private fun registerBlockVariants() {
        if (!AdditionsConfig.EnabledBlockVariants) {
            logger.info("Block Variants disabled, using fallback blocks for properties.")
            // Hier musst du nichts tun, die Vars haben ja schon Fallbacks.
            return
        } else {
            logger.info("Starting block variant registration")
            // Varianten für jeden Basis-Block erstellen
            blockVariantsParents.forEach { parent ->
                val baseName =
                    Registries.BLOCK
                        .getId(parent)
                        .path
                        .replace("_block", "")
                val settings = AbstractBlock.Settings.copy(parent)

                logger.debug("Creating variants for base block: $baseName")

                // Platten-Variante registrieren
                val slab =
                    when (parent) {
                        is GrassBlock -> {
                            register(
                                "${baseName}_slab",
                                SnowySlabBlock(
                                    settings.registryKey(keyOf("${baseName}_slab")),
                                ),
                            ).also { registeredGrassBlocks.add(it) }
                        }
                        Blocks.PODZOL, Blocks.MYCELIUM -> {
                            register(
                                "${baseName}_slab",
                                SnowySlabBlock(
                                    settings.registryKey(keyOf("${baseName}_slab")),
                                ),
                            )
                        }
                        is CryingObsidianBlock -> {
                            register(
                                "${baseName}_slab",
                                CryingObsidianSlab(
                                    settings.registryKey(keyOf("${baseName}_slab")),
                                ),
                            )
                        }
                        is DirtPathBlock -> {
                            register(
                                "${baseName}_slab",
                                PathSlab(
                                    settings.registryKey(keyOf("${baseName}_slab")),
                                ),
                            ).also {
                                DIRT_PATH_SLAB = it
                                registeredDirtBlockVariants.add(it)
                            }
                        }
                        Blocks.DIRT -> {
                            register(
                                "${baseName}_slab",
                                SlabBlock(
                                    settings.registryKey(keyOf("${baseName}_slab")),
                                ),
                            ).also {
                                DIRT_SLAB = it
                                registeredDirtBlockVariants.add(it)
                            }
                        }
                        is MagmaBlock -> {
                            register(
                                "${baseName}_slab",
                                MagmaSlab(
                                    settings.registryKey(keyOf("${baseName}_slab")),
                                ),
                            ).also {
                                registeredMagmaBlockVariants.add(it)
                            }
                        }
                        else -> {
                            register(
                                "${baseName}_slab",
                                SlabBlock(
                                    settings.registryKey(keyOf("${baseName}_slab")),
                                ),
                            ).also { if (parent == Blocks.DIRT) registeredDirtBlockVariants.add(it) }
                        }
                    }

                // Treppen-Variante registrieren
                val stair =
                    when (parent) {
                        is GrassBlock ->
                            register(
                                "${baseName}_stairs",
                                SnowyStairsBlock(
                                    parent.defaultState,
                                    settings.registryKey(keyOf("${baseName}_stairs")),
                                ),
                            ).also { registeredGrassBlocks.add(it) }

                        Blocks.PODZOL, Blocks.MYCELIUM ->
                            register(
                                "${baseName}_stairs",
                                SnowyStairsBlock(
                                    parent.defaultState,
                                    settings.registryKey(keyOf("${baseName}_stairs")),
                                ),
                            )
                        is CryingObsidianBlock ->
                            register(
                                "${baseName}_stairs",
                                CryingObsidianStair(
                                    parent.defaultState,
                                    settings.registryKey(keyOf("${baseName}_stairs")),
                                ),
                            )
                        is MagmaBlock ->
                            register(
                                "${baseName}_stairs",
                                MagmaStair(
                                    parent.defaultState,
                                    settings.registryKey(keyOf("${baseName}_stairs")),
                                ),
                            ).also { registeredMagmaBlockVariants.add(it) }
                        is DirtPathBlock ->
                            register(
                                "${baseName}_stairs",
                                PathStair(
                                    parent.defaultState,
                                    settings.registryKey(keyOf("${baseName}_stairs")),
                                ),
                            ).also {
                                DIRT_PATH_STAIRS = it
                                registeredDirtBlockVariants.add(it)
                            }
                        Blocks.DIRT ->
                            register(
                                "${baseName}_stairs",
                                StairsBlock(
                                    parent.defaultState,
                                    settings.registryKey(keyOf("${baseName}_stairs")),
                                ),
                            ).also {
                                DIRT_STAIRS = it
                                registeredDirtBlockVariants.add(it)
                            }
                        else ->
                            register(
                                "${baseName}_stairs",
                                StairsBlock(
                                    parent.defaultState,
                                    settings.registryKey(keyOf("${baseName}_stairs")),
                                ),
                            ).also { if (parent == Blocks.DIRT) registeredDirtBlockVariants.add(it) }
                    }
                registeredStairs.add(stair)
                registeredSlabs.add(slab)
            }

            logger.info("Block variant registration completed")
        }
    }

    /**
     * Registriert Laternen- und Ketten-Varianten für vorgegebene Basis-Blöcke.
     */
    private fun registerLanternVariants() {
        logger.info("Starting lantern variant registration")

        lanternVariantsParents.forEach { baseBlock ->
            var lantern: Block? = null
            var redstoneLantern: Block? = null
            var bigLantern: Block? = null
            var bigRedstoneLantern: Block? = null
            var smallLantern: Block? = null
            var smallRedstoneLantern: Block? = null

            val baseName =
                Registries.BLOCK
                    .getId(baseBlock)
                    .path
                    .replace("_block", "")
            val lanternSettings = AbstractBlock.Settings.copy(Blocks.LANTERN)

            logger.debug("Creating lantern variants for base block: $baseName")

            // Laternen-Variante registrieren
            if (baseBlock != Blocks.IRON_BLOCK) {
                lantern =
                    register(
                        "${baseName}_lantern",
                        LanternBlock(
                            lanternSettings.registryKey(keyOf("${baseName}_lantern")),
                        ),
                    )
            }

            bigLantern =
                register(
                    "${baseName}_big_lantern",
                    BigLantern(
                        lanternSettings.registryKey(keyOf("${baseName}_big_lantern")),
                    ),
                )
            smallLantern =
                register(
                    "${baseName}_small_lantern",
                    SmallLantern(
                        lanternSettings.registryKey(keyOf("${baseName}_small_lantern")),
                    ),
                )

            // Redstone-Varianten Logging
            if (AdditionsConfig.EnabledRedstoneLantern) {
                logger.warn("Redstone Lanterns are enabled. Variant registration for: $baseName")
                redstoneLantern =
                    register(
                        "${baseName}_redstone_lantern",
                        RedstoneLantern(
                            lanternSettings.registryKey(keyOf("${baseName}_redstone_lantern")).luminance(
                                Blocks.createLightLevelFromLitBlockState(
                                    15,
                                ),
                            ),
                        ),
                    )
                bigRedstoneLantern =
                    register(
                        "${baseName}_big_redstone_lantern",
                        BigRedstoneLantern(
                            lanternSettings.registryKey(keyOf("${baseName}_big_redstone_lantern")).luminance(
                                Blocks.createLightLevelFromLitBlockState(
                                    15,
                                ),
                            ),
                        ),
                    )
                smallRedstoneLantern =
                    register(
                        "${baseName}_small_redstone_lantern",
                        SmallRedstoneLantern(
                            lanternSettings.registryKey(keyOf("${baseName}_small_redstone_lantern")).luminance(
                                Blocks.createLightLevelFromLitBlockState(
                                    15,
                                ),
                            ),
                        ),
                    )
            } else {
                logger.debug("Redstone Lanterns disabled for $baseName")
            }
            lantern?.let { registeredLanterns[it] = baseBlock }
            redstoneLantern?.let { registeredLanterns[it] = baseBlock }
            bigLantern.let { registeredLanterns[it] = baseBlock }
            bigRedstoneLantern?.let { registeredLanterns[it] = baseBlock }
            smallLantern.let { registeredLanterns[it] = baseBlock }
            smallRedstoneLantern?.let { registeredLanterns[it] = baseBlock }
        }

        logger.info("Lantern variant registration completed")
    }

    /**
     * Registriert Falltür-Varianten für vorgegebene Basis-Blöcke.
     */
    private fun registerTrapdoorVariants() {
        logger.info("Starting trapdoor variant registration")

        trapdoorVariantsParents.forEach { baseBlock ->
            val baseName =
                Registries.BLOCK
                    .getId(baseBlock)
                    .path
                    .replace("_block", "")
            val trapdoorSettings = AbstractBlock.Settings.copy(baseBlock)

            logger.debug("Creating trapdoor variants for base block: $baseName")

            // Falltür-Variante registrieren
            val trapdoor =
                register(
                    "${baseName}_trapdoor",
                    TrapdoorBlock(
                        BlockSetType.OAK,
                        trapdoorSettings.registryKey(keyOf("${baseName}_trapdoor")),
                    ),
                )

            registeredTrapdoors.add(trapdoor)
        }
        logger.info("Trapdoor variant registration completed")
    }

    /**
     * Register Chains
     */
    private fun registerChains() {
        logger.info("Starting chain registration")
        val chainSettings = AbstractBlock.Settings.copy(Blocks.CHAIN)
        val chain = register("redstone_chain", RedstoneChainBlock(chainSettings.registryKey(keyOf("redstone_chain"))))
        registeredChains.add(chain)
        logger.info("Chain registration completed")
    }
}
