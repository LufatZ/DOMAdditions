/**
 * Registry for additional Minecraft blocks and their variants.
 * Enables automatic registration of stairs, slabs, and lantern variants.
 */
@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.blocks

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry.blockVariantsParents
import de.additions.blocks.BlockRegistry.lanternVariantsParents
import de.additions.blocks.BlockRegistry.trapdoorVariantsParents
import de.additions.blocks.lanterns.*
import de.additions.config.AdditionsConfig
import de.additions.itemGroups.ItemGroupRegistry
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.CryingObsidianBlock
import net.minecraft.world.level.block.DirtPathBlock
import net.minecraft.world.level.block.GrassBlock
import net.minecraft.world.level.block.LanternBlock
import net.minecraft.world.level.block.MagmaBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.level.block.WeatheringCopperBlocks
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType

/**
 * Handles the registration of all custom blocks and their variants.
 * This object manages the creation and registration of stairs, slabs, lanterns, trapdoors, and chains
 * based on configuration settings and predefined parent blocks.
 */
object BlockRegistry {
    /** Stores all registered blocks as ItemStacks for later use, e.g., in item groups. */
    val registeredBlocks: MutableList<ItemStack> = mutableListOf()

    /** List of all registered stair blocks. */
    @JvmStatic
    val registeredStairs: MutableList<Block> = mutableListOf()

    /** List of all registered slab blocks. */
    @JvmStatic
    val registeredSlabs: MutableList<Block> = mutableListOf()

    /** A map of registered lantern variants to their parent blocks. */
    val registeredLanterns: MutableMap<Block, Block> = mutableMapOf()

    /** List of all registered trapdoor blocks. */
    val registeredTrapdoors: MutableList<Block> = mutableListOf()

    /** List of all registered chain blocks. */
    val registeredChains: MutableList<Block> = mutableListOf()

    /** List of all registered grass block variants (stairs and slabs). */
    val registeredGrassBlocks: MutableList<Block> = mutableListOf()

    /** List of all registered dirt block variants. */
    private val registeredDirtBlockVariants: MutableList<Block> = mutableListOf()

    /** List of all registered magma block variants. */
    private val registeredMagmaBlockVariants: MutableList<Block> = mutableListOf()

    /** The registered dirt path stair block, with a fallback to oak stairs. */
    @Suppress("ktlint:standard:property-naming")
    var DIRT_PATH_STAIR: Block = Blocks.OAK_STAIRS // Fallback-Block
        private set

    /** The registered dirt path slab block, with a fallback to oak slab. */
    @Suppress("ktlint:standard:property-naming")
    var DIRT_PATH_SLAB: Block = Blocks.OAK_SLAB // Fallback-Block
        private set

    /** The registered dirt stair block, with a fallback to oak stairs. */
    @Suppress("ktlint:standard:property-naming")
    var DIRT_STAIR: Block = Blocks.OAK_STAIRS // Fallback-Block
        private set

    /** The registered dirt slab block, with a fallback to oak slab. */
    @Suppress("ktlint:standard:property-naming")
    var DIRT_SLAB: Block = Blocks.OAK_SLAB // Fallback-Block
        private set

    /**
     * The list of parent blocks for which stair and slab variants will be created.
     * This list is conditional on the `EnabledBlockVariants` configuration setting.
     */
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

    /**
     * The list of parent blocks for which lantern variants will be created.
     * This list is conditional on the `EnabledLantern` configuration setting.
     */
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

    /**
     * The list of parent blocks for which trapdoor variants will be created.
     * This list is conditional on the `EnabledTrapdoor` configuration setting.
     */
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

    /**
     * Gets the list of registered magma block variants.
     * Used by the BubbleColumnBlockMixin to create bubble columns.
     * @return A list of magma block variants.
     */
    @JvmStatic
    fun getRegisteredMagmaBlocks() = registeredMagmaBlockVariants.toList()

    /**
     * The main registration method for all custom blocks.
     * This function orchestrates the registration of all block variants based on the configuration.
     */
    fun registerAllBlocks() {
        logger.info("Initiating block registration process")
        registerBlockVariants()
        registerLanternVariants()
        registerTrapdoorVariants()
        registerChains()
        logger.info("Block registration completed. Total registered blocks: ${registeredBlocks.size}")

        // Add all registered blocks to the default item groups
        ItemGroupRegistry.registerItemsAfterCommonParent(registeredLanterns.keys.toList(), Blocks.LANTERN)
        ItemGroupRegistry.registerItemsAfterCommonParent(registeredChains, Blocks.IRON_CHAIN)
        ItemGroupRegistry.registerBlocksInDefaultGroups(registeredTrapdoors, trapdoorVariantsParents)
        ItemGroupRegistry.registerBlocksInDefaultGroups(registeredSlabs, blockVariantsParents)
        ItemGroupRegistry.registerBlocksInDefaultGroups(registeredStairs, blockVariantsParents)

        //blockDoesNNotWork()
    }

    /**
     * Creates a [RegistryKey] with flexible options.
     * Supports both vanilla and mod-specific resources.
     *
     * @param T The type of the registry entry (e.g., Block, Item).
     * @param id The identifier string for the resource.
     * @param vanilla Whether the resource is a vanilla Minecraft resource.
     * @param type The specific [RegistryKey] for the registry type.
     * @return The created [RegistryKey].
     * @throws IllegalArgumentException if the type T is not supported.
     */
    private inline fun <reified T : Any> keyOf(
        id: String,
        vanilla: Boolean = true,
        type: ResourceKey<Registry<T>>? = null,
    ): ResourceKey<T> {
        @Suppress("UNCHECKED_CAST")
        val resolvedType =
            type ?: when (T::class) {
                Block::class -> Registries.BLOCK as ResourceKey<Registry<T>>
                Item::class -> Registries.ITEM as ResourceKey<Registry<T>>
                else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
            }

        val identifier =
            if (vanilla) {
                logger.debug("Creating Vanilla registry key for: $id")
                Identifier.withDefaultNamespace(id)
            } else {
                logger.debug("Creating Mod-specific registry key for: $id")
                Identifier.fromNamespaceAndPath(MODID, id)
            }

        return ResourceKey.create(resolvedType, identifier)
    }

    /**
     * Registers a block and its corresponding [BlockItem].
     *
     * @param id The identifier for the block (without namespace).
     * @param block The block instance to register.
     * @return The registered block instance.
     */
    private fun register(
        id: String,
        block: Block,
    ): Block {
        logger.debug("Registering block variant: $id")

        val blockKey = keyOf(id = id, type = Registries.BLOCK)
        val itemKey = keyOf(id = id, type = Registries.ITEM)

        // Register the block and its BlockItem
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block)
        val blockItem = BlockItem(block, Item.Properties().setId(itemKey))
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem)

        // Add the ItemStack to the list for item groups
        registeredBlocks.add(ItemStack(blockItem))

        logger.debug("Successfully registered block and block item: $id")
        return block
    }

    /**
     * Registers stair and slab variants for the parent blocks defined in [blockVariantsParents].
     */
    private fun registerBlockVariants() {
        if (!AdditionsConfig.EnabledBlockVariants) {
            logger.info("Block Variants disabled, using fallback blocks for properties.")
            // Nothing to do here, the properties already have fallbacks.
            return
        } else {
            logger.info("Starting block variant registration")
            // Create variants for each parent block
            blockVariantsParents.forEach { parent ->
                val baseName =
                    BuiltInRegistries.BLOCK
                        .getKey(parent)
                        .path
                        .replace("_block", "")
                val settings = BlockBehaviour.Properties.ofFullCopy(parent)

                logger.debug("Creating variants for base block: $baseName")

                // Register slab variant
                val slab =
                    when (parent) {
                        is GrassBlock -> {
                            register(
                                "${baseName}_slab",
                                SnowySlabBlock(
                                    settings.setId(keyOf("${baseName}_slab")),
                                ),
                            ).also { registeredGrassBlocks.add(it) }
                        }
                        Blocks.PODZOL, Blocks.MYCELIUM -> {
                            register(
                                "${baseName}_slab",
                                SnowySlabBlock(
                                    settings.setId(keyOf("${baseName}_slab")),
                                ),
                            )
                        }
                        is CryingObsidianBlock -> {
                            register(
                                "${baseName}_slab",
                                CryingObsidianSlab(
                                    settings.setId(keyOf("${baseName}_slab")),
                                ),
                            )
                        }
                        is DirtPathBlock -> {
                            register(
                                "${baseName}_slab",
                                PathSlab(
                                    settings.setId(keyOf("${baseName}_slab")),
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
                                    settings.setId(keyOf("${baseName}_slab")),
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
                                    settings.setId(keyOf("${baseName}_slab")),
                                ),
                            ).also {
                                registeredMagmaBlockVariants.add(it)
                            }
                        }
                        else -> {
                            register(
                                "${baseName}_slab",
                                SlabBlock(
                                    settings.setId(keyOf("${baseName}_slab")),
                                ),
                            ).also { if (parent == Blocks.DIRT) registeredDirtBlockVariants.add(it) }
                        }
                    }

                // Register stair variant
                val stair =
                    when (parent) {
                        is GrassBlock ->
                            register(
                                "${baseName}_stairs",
                                SnowyStairsBlock(
                                    parent.defaultBlockState(),
                                    settings.setId(keyOf("${baseName}_stairs")),
                                ),
                            ).also { registeredGrassBlocks.add(it) }

                        Blocks.PODZOL, Blocks.MYCELIUM ->
                            register(
                                "${baseName}_stairs",
                                SnowyStairsBlock(
                                    parent.defaultBlockState(),
                                    settings.setId(keyOf("${baseName}_stairs")),
                                ),
                            )
                        is CryingObsidianBlock ->
                            register(
                                "${baseName}_stairs",
                                CryingObsidianStair(
                                    parent.defaultBlockState(),
                                    settings.setId(keyOf("${baseName}_stairs")),
                                ),
                            )
                        is MagmaBlock ->
                            register(
                                "${baseName}_stairs",
                                MagmaStair(
                                    parent.defaultBlockState(),
                                    settings.setId(keyOf("${baseName}_stairs")),
                                ),
                            ).also { registeredMagmaBlockVariants.add(it) }
                        is DirtPathBlock ->
                            register(
                                "${baseName}_stairs",
                                PathStair(
                                    parent.defaultBlockState(),
                                    settings.isSuffocating(Blocks::never).setId(keyOf("${baseName}_stairs")),
                                ),
                            ).also {
                                DIRT_PATH_STAIR = it
                                registeredDirtBlockVariants.add(it)
                            }
                        Blocks.DIRT ->
                            register(
                                "${baseName}_stairs",
                                StairBlock(
                                    parent.defaultBlockState(),
                                    settings.setId(keyOf("${baseName}_stairs")),
                                ),
                            ).also {
                                DIRT_STAIR = it
                                registeredDirtBlockVariants.add(it)
                            }
                        else ->
                            register(
                                "${baseName}_stairs",
                                StairBlock(
                                    parent.defaultBlockState(),
                                    settings.setId(keyOf("${baseName}_stairs")),
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
     * Registers lantern variants for the parent blocks defined in [lanternVariantsParents].
     * This includes standard, big, small, and redstone-powered versions.
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
            var copperSmallLanternSet: WeatheringCopperBlocks? = null
            var copperBigLanternSet: WeatheringCopperBlocks? = null
            var copperSmallRedstoneLanternSet: WeatheringCopperBlocks? = null
            var copperBigRedstoneLanternSet: WeatheringCopperBlocks? = null
            var copperRedstoneLanternSet: WeatheringCopperBlocks? = null

            val baseName =
                BuiltInRegistries.BLOCK
                    .getKey(baseBlock)
                    .path
                    .replace("_block", "")
            val lanternSettings = BlockBehaviour.Properties.ofFullCopy(Blocks.LANTERN)

            logger.debug("Creating lantern variants for base block: $baseName")
            if (baseBlock != Blocks.COPPER_BLOCK) {
                // Register standard lantern variant
                if (baseBlock != Blocks.IRON_BLOCK) {
                    lantern =
                        register(
                            "${baseName}_lantern",
                            LanternBlock(
                                lanternSettings.setId(keyOf("${baseName}_lantern")),
                            ),
                        )
                }

                bigLantern =
                    register(
                        "${baseName}_big_lantern",
                        BigLantern(
                            lanternSettings.setId(keyOf("${baseName}_big_lantern")),
                        ),
                    )
                smallLantern =
                    register(
                        "${baseName}_small_lantern",
                        SmallLantern(
                            lanternSettings.setId(keyOf("${baseName}_small_lantern")),
                        ),
                    )
            } else {

                // Standard Laterns not needed (already added by default minecraft)
                // Große Laternen
                copperBigLanternSet = WeatheringCopperBlocks.create(
                    "${baseName}_big_lantern",
                    { id, factory, settings ->
                        register(id, factory.apply(settings.setId(keyOf(id))))
                    },
                    { settings -> BigLantern(settings) },
                    { oxidationLevel, settings -> BigOxidizableLantern(oxidationLevel, settings) },
                    { _ -> lanternSettings }
                )

                // Kleine Laternen
                copperSmallLanternSet = WeatheringCopperBlocks.create(
                    "${baseName}_small_lantern",
                    { id, factory, settings ->
                        register(id, factory.apply(settings.setId(keyOf(id))))
                    },
                    { settings -> SmallLantern(settings) },
                    { oxidationLevel, settings -> SmallOxidizableLantern(oxidationLevel, settings) },
                    { _ -> lanternSettings }
                )
            }
            // Register redstone variants if enabled
            if (AdditionsConfig.EnabledRedstoneLantern) {
                logger.warn("Redstone Lanterns are enabled. Variant registration for: $baseName")
                if (baseBlock != Blocks.COPPER_BLOCK) {
                    redstoneLantern =
                        register(
                            "${baseName}_redstone_lantern",
                            RedstoneLantern(
                                lanternSettings.setId(keyOf("${baseName}_redstone_lantern")).lightLevel(
                                    Blocks.litBlockEmission(
                                        15,
                                    ),
                                ),
                            ),
                        )
                    bigRedstoneLantern =
                        register(
                            "${baseName}_big_redstone_lantern",
                            BigRedstoneLantern(
                                lanternSettings.setId(keyOf("${baseName}_big_redstone_lantern")).lightLevel(
                                    Blocks.litBlockEmission(
                                        15,
                                    ),
                                ),
                            ),
                        )
                    smallRedstoneLantern =
                        register(
                            "${baseName}_small_redstone_lantern",
                            SmallRedstoneLantern(
                                lanternSettings.setId(keyOf("${baseName}_small_redstone_lantern")).lightLevel(
                                    Blocks.litBlockEmission(
                                        15,
                                    ),
                                ),
                            ),
                        )
                } else {
                    // Big Lantern
                    copperBigRedstoneLanternSet = WeatheringCopperBlocks.create(
                        "${baseName}_big_redstone_lantern",
                        { id, factory, settings ->
                            register(id, factory.apply(settings.setId(keyOf(id))))
                        },
                        { settings ->
                            BigRedstoneLantern(settings.lightLevel(
                                Blocks.litBlockEmission(15)))
                        },
                        { oxidationLevel, settings ->
                            BigOxidizableRedstoneLantern(
                                oxidationLevel,
                                settings.lightLevel(Blocks.litBlockEmission(15))
                            )
                        },
                        { _ -> lanternSettings }
                    )

                    // Small Lantern
                    copperSmallRedstoneLanternSet = WeatheringCopperBlocks.create(
                        "${baseName}_small_redstone_lantern",
                        { id, factory, settings ->
                            register(id, factory.apply(settings.setId(keyOf(id))))
                        },
                        { settings ->
                            SmallRedstoneLantern(settings.lightLevel(
                                Blocks.litBlockEmission(15)))
                        },
                        { oxidationLevel, settings ->
                            SmallOxidizableRedstoneLantern(
                                oxidationLevel,
                                settings.lightLevel(Blocks.litBlockEmission(15))
                            )
                        },
                        { _ -> lanternSettings }
                    )

                    // Default Lantern
                    copperRedstoneLanternSet = WeatheringCopperBlocks.create(
                        "${baseName}_redstone_lantern",
                        { id, factory, settings ->
                            register(id, factory.apply(settings.setId(keyOf(id))))
                        },
                        { settings ->
                            RedstoneLantern(settings.lightLevel(
                                Blocks.litBlockEmission(15)))
                        },
                        { oxidationLevel, settings ->
                            OxidizableRedstoneLantern(
                                oxidationLevel,
                                settings.lightLevel(Blocks.litBlockEmission(15))
                            )
                        },
                        { _ -> lanternSettings }
                    )
                }
            } else {
                logger.debug("Redstone Lanterns disabled for $baseName")
            }
            lantern?.let { registeredLanterns[it] = baseBlock }
            redstoneLantern?.let { registeredLanterns[it] = baseBlock }
            bigLantern?.let { registeredLanterns[it] = baseBlock }
            bigRedstoneLantern?.let { registeredLanterns[it] = baseBlock }
            smallLantern?.let { registeredLanterns[it] = baseBlock }
            smallRedstoneLantern?.let { registeredLanterns[it] = baseBlock }

            // Add all copper lantern variants to the map
            copperBigLanternSet?.forEach { block -> registeredLanterns[block] = baseBlock }
            copperSmallLanternSet?.forEach { block -> registeredLanterns[block] = baseBlock }
            copperBigRedstoneLanternSet?.forEach { block -> registeredLanterns[block] = baseBlock }
            copperSmallRedstoneLanternSet?.forEach { block -> registeredLanterns[block] = baseBlock }
            copperRedstoneLanternSet?.forEach { block -> registeredLanterns[block] = baseBlock }
        }

        logger.info("Lantern variant registration completed")
    }

    /**
     * Registers trapdoor variants for the parent blocks defined in [trapdoorVariantsParents].
     */
    private fun registerTrapdoorVariants() {
        logger.info("Starting trapdoor variant registration")

        trapdoorVariantsParents.forEach { baseBlock ->
            val baseName =
                BuiltInRegistries.BLOCK
                    .getKey(baseBlock)
                    .path
                    .replace("_block", "")
            val trapdoorSettings = BlockBehaviour.Properties.ofFullCopy(baseBlock)

            logger.debug("Creating trapdoor variants for base block: $baseName")

            // Register trapdoor variant
            val trapdoor =
                register(
                    "${baseName}_trapdoor",
                    TrapDoorBlock(
                        BlockSetType.OAK,
                        trapdoorSettings.setId(keyOf("${baseName}_trapdoor")),
                    ),
                )

            registeredTrapdoors.add(trapdoor)
        }
        logger.info("Trapdoor variant registration completed")
    }

    /**
     * Registers custom chain blocks.
     */
    private fun registerChains() {
        logger.info("Starting chain registration")
        val chainSettings = BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_CHAIN)
        val chain = register("redstone_chain", RedstoneChainBlock(chainSettings.setId(keyOf("redstone_chain"))))
        val copperChain = WeatheringCopperBlocks.create(
            "copper_redstone_chain",
            { id, factory, settings ->
                register(id, factory.apply(settings.setId(keyOf(id))))
            },
            { settings -> RedstoneChainBlock(settings) },
            { oxidationLevel, settings -> OxidizableRedstoneChainBlock(oxidationLevel, settings) },
            { _ -> chainSettings }
        )
        registeredChains.add(chain)
        copperChain.forEach { block -> registeredChains.add(block) }
        logger.info("Chain registration completed")
    }
}
