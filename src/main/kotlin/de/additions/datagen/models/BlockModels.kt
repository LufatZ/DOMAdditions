@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen.models

import de.additions.Additions.MODID
import de.additions.blocks.*
import de.additions.blocks.BlockRegistry.blockVariantsParents
import de.additions.blocks.BlockRegistry.registeredChains
import de.additions.blocks.BlockRegistry.registeredLanterns
import de.additions.blocks.BlockRegistry.registeredSlabs
import de.additions.blocks.BlockRegistry.registeredStairs
import de.additions.blocks.BlockRegistry.registeredTrapdoors
import de.additions.blocks.BlockRegistry.trapdoorVariantsParents
import de.additions.blocks.lanterns.BigLantern
import de.additions.blocks.lanterns.BigRedstoneLantern
import de.additions.blocks.lanterns.RedstoneLantern
import de.additions.blocks.lanterns.SmallLantern
import de.additions.blocks.lanterns.SmallRedstoneLantern
import de.additions.datagen.models.CustomStates.createCustomStairsBlockState
import de.additions.datagen.models.CustomStates.createSnowySlabBlockState
import de.additions.datagen.models.CustomStates.createStairsModelMap
import de.additions.datagen.models.ModelGenerator.Companion.hasNoTexture
import de.additions.datagen.models.ModelGenerator.Companion.hasSideAndTop
import de.additions.datagen.models.ModelGenerator.Companion.removeBlock
import de.additions.datagen.models.ModelGenerator.Companion.snowyOvergrownBlocks
import de.additions.helper.ModelHelper.configureBlockTextureMapping
import de.additions.helper.ModelHelper.extractCleanBlockIdentifier
import de.additions.helper.ModelHelper.generateBlockItemModel
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.DirtPathBlock
import net.minecraft.block.GrassBlock
import net.minecraft.client.data.*
import net.minecraft.client.data.BlockStateModelGenerator.*
import net.minecraft.registry.Registries
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import java.util.*

object BlockModels {
    lateinit var generator: BlockStateModelGenerator

    fun BlockStateModelGenerator?.init() {
        generator = this!!

        generateStairModels()
        generateSlabModels()
        generateLanternModels()
        generateTrapdoorModels()
        generateChainModels()
    }

    /**
     * Processes and generates models for all registered stairs.
     *
     * Handles special cases for certain parent blocks like Podzol and Mycelium,
     * which require custom bottom texture handling.
     */
    private fun generateStairModels() {
        registeredStairs.forEachIndexed { index, stair ->
            when (val parentBlock = blockVariantsParents[index]) {
                Blocks.PODZOL -> generateStairModel(stair, parentBlock, bottom = Blocks.DIRT)
                Blocks.MYCELIUM -> generateStairModel(stair, parentBlock, bottom = Blocks.DIRT)
                Blocks.DIRT_PATH -> generateStairModel(stair, parentBlock, bottom = Blocks.DIRT)
                Blocks.GRASS_BLOCK -> generateStairModel(stair, parentBlock, bottom = Blocks.DIRT)
                else -> generateStairModel(stair, parentBlock)
            }
        }
    }

    /**
     * Processes and generates models for all registered slabs.
     *
     * Similar to stair generation, handles special cases for parent blocks
     * that require custom texture mapping.
     */
    private fun generateSlabModels() {
        registeredSlabs.forEachIndexed { index, slab ->
            when (val parentBlock = blockVariantsParents[index]) {
                Blocks.PODZOL -> generateSlabModel(slab, parentBlock, bottom = Blocks.DIRT)
                Blocks.MYCELIUM -> generateSlabModel(slab, parentBlock, bottom = Blocks.DIRT)
                Blocks.DIRT_PATH -> generateSlabModel(slab, parentBlock, bottom = Blocks.DIRT)
                Blocks.GRASS_BLOCK -> generateSlabModel(slab, parentBlock, bottom = Blocks.DIRT)
                else -> generateSlabModel(slab, parentBlock)
            }
        }
    }

    /**
     * Processes and generates models for all registered lanterns.
     *
     * Creates standing and hanging variants for each registered lantern,
     * using the parent block's texture as a base.
     */
    private fun generateLanternModels() {
        registeredLanterns.forEach { (lantern, baseBlock) -> generateLanternModel(lantern, baseBlock) }
    }

    /**
     * Processes and generates models for all registered trapdoors.
     *
     * Creates bottom, top, and open variants for each registered trapdoor,
     * using the parent block's texture.
     */
    private fun generateTrapdoorModels() {
        registeredTrapdoors.forEachIndexed { index, trapdoor ->
            when (val parentBlock = trapdoorVariantsParents[index]) {
                else -> generateTrapdoorModel(trapdoor, parentBlock)
            }
        }
    }

    /**
     * Generates models for all chain variants.
     */
    private fun generateChainModels() {

        registeredChains.forEach { chain ->
            val id = Registries.BLOCK.getId(chain).path
            val chainModelId =
                if (id.contains("copper"))
                    when {
                        id.contains("oxidized_") -> Identifier.ofVanilla("block/oxidized_copper_chain")
                        id.contains("weathered_") -> Identifier.ofVanilla("block/weathered_copper_chain")
                        id.contains("exposed_") -> Identifier.ofVanilla("block/exposed_copper_chain")
                        else -> Identifier.ofVanilla("block/copper_chain")
                    } else {
                    Identifier.ofVanilla("block/iron_chain")
                }
            val chainItemModelId =
                if (id.contains("copper"))
                    when {
                        id.contains("oxidized_") -> Identifier.ofVanilla("item/oxidized_copper_chain")
                        id.contains("weathered_") -> Identifier.ofVanilla("item/weathered_copper_chain")
                        id.contains("exposed_") -> Identifier.ofVanilla("item/exposed_copper_chain")
                        else -> Identifier.ofVanilla("item/copper_chain")
                    } else {
                    Identifier.ofVanilla("item/iron_chain")
                }


            generator.registerAxisRotated(chain, createWeightedVariant(chainModelId))
            generator.registerParentedItemModel(chain, chainItemModelId)
        }
    }

    /**
     * Generates all necessary models and blockstates for a stair variant.
     */
    private fun generateStairModel(
        stair: Block,
        parent: Block,
        top: Block = parent,
        side: Block = parent,
        bottom: Block = parent,
    ) {
        val textureMap =
            configureBlockTextureMapping(
                parent = parent,
                top = top,
                side = side,
                bottom = bottom,
                hasSideAndTop = parent in hasSideAndTop,
                removeBlock = parent in removeBlock,
                bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
                textureKey = if (parent is GrassBlock) "overlay" else "",
            )

        val snowyTextureMap =
            configureBlockTextureMapping(
                parent = parent,
                top = Blocks.SNOW,
                side = if (parent in snowyOvergrownBlocks) Blocks.GRASS_BLOCK else side,
                bottom = bottom,
                hasSideAndTop = parent in hasSideAndTop,
                removeBlock = parent in removeBlock,
                bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
                textureKey = "",
            )

        // Regular models
        val regularModel =
            when (parent) {
                is GrassBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                        TextureKey.LAYER0,
                    ).upload(stair, "", textureMap, generator.modelCollector)

                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_overgrown_stair")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "", textureMap, generator.modelCollector)

                is DirtPathBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_path_stair")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "", textureMap, generator.modelCollector)

                else -> Models.STAIRS.upload(stair, "", textureMap, generator.modelCollector)
            }

        val regularModelRotated =
            when (parent) {
                is GrassBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                        TextureKey.LAYER0,
                    ).upload(stair, "_rotated", textureMap, generator.modelCollector)

                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_overgrown_stair_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_rotated", textureMap, generator.modelCollector)

                is DirtPathBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_path_stair_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_rotated", textureMap, generator.modelCollector)

                else -> Models.STAIRS.upload(stair, "_rotated", textureMap, generator.modelCollector)
            }

        val outerModel =
            when (parent) {
                is GrassBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_outer")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                        TextureKey.LAYER0,
                    ).upload(stair, "_outer", textureMap, generator.modelCollector)

                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_overgrown_stair_outer")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_outer", textureMap, generator.modelCollector)

                is DirtPathBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_path_stair_outer")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_outer", textureMap, generator.modelCollector)

                else -> Models.OUTER_STAIRS.upload(stair, "", textureMap, generator.modelCollector)
            }

        val outerModelRotated =
            when (parent) {
                is GrassBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_outer_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                        TextureKey.LAYER0,
                    ).upload(stair, "_outer_rotated", textureMap, generator.modelCollector)

                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_overgrown_stair_outer_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_outer_rotated", textureMap, generator.modelCollector)

                is DirtPathBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_path_stair_outer_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_outer_rotated", textureMap, generator.modelCollector)

                else -> Models.OUTER_STAIRS.upload(stair, "_rotated", textureMap, generator.modelCollector)
            }

        val innerModel =
            when (parent) {
                is GrassBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_inner")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                        TextureKey.LAYER0,
                    ).upload(stair, "_inner", textureMap, generator.modelCollector)

                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_overgrown_stair_inner")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_inner", textureMap, generator.modelCollector)

                is DirtPathBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_path_stair_inner")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_inner", textureMap, generator.modelCollector)

                else -> Models.INNER_STAIRS.upload(stair, "", textureMap, generator.modelCollector)
            }

        val innerModelRotated =
            when (parent) {
                is GrassBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_inner_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                        TextureKey.LAYER0,
                    ).upload(stair, "_inner_rotated", textureMap, generator.modelCollector)

                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_overgrown_stair_inner_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_inner_rotated", textureMap, generator.modelCollector)

                is DirtPathBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_path_stair_inner_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_inner_rotated", textureMap, generator.modelCollector)

                else -> Models.INNER_STAIRS.upload(stair, "_rotated", textureMap, generator.modelCollector)
            }

        // Snowy models for grass blocks
        val regularSnowyModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_snowy")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_snowy", snowyTextureMap, generator.modelCollector)

                else -> null
            }

        val regularSnowyModelRotated =
            when (parent) {
                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_snowy_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_snowy_rotated", snowyTextureMap, generator.modelCollector)

                else -> null
            }

        val outerSnowyModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_outer_snowy")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_snowy_outer", snowyTextureMap, generator.modelCollector)

                else -> null
            }

        val outerSnowyModelRotated =
            when (parent) {
                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_outer_snowy_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_snowy_outer_rotated", snowyTextureMap, generator.modelCollector)

                else -> null
            }

        val innerSnowyModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_inner_snowy")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_snowy_inner", snowyTextureMap, generator.modelCollector)

                else -> null
            }

        val innerSnowyModelRotated =
            when (parent) {
                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_stair_inner_snowy_rotated")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(stair, "_snowy_inner_rotated", snowyTextureMap, generator.modelCollector)

                else -> null
            }

        generateBlockItemModel(stair, parent, regularModel, generator)

        generator.blockStateCollector?.accept(
            when (parent) {
                in snowyOvergrownBlocks ->
                    createCustomStairsBlockState(
                        stair,
                        createStairsModelMap(
                            createWeightedVariant(innerModel),
                            createWeightedVariant(regularModel),
                            createWeightedVariant(outerModel),
                            createWeightedVariant(innerModelRotated),
                            createWeightedVariant(regularModelRotated),
                            createWeightedVariant(outerModelRotated),
                            createWeightedVariant(innerSnowyModel),
                            createWeightedVariant(regularSnowyModel),
                            createWeightedVariant(outerSnowyModel),
                            createWeightedVariant(innerSnowyModelRotated),
                            createWeightedVariant(regularSnowyModelRotated),
                            createWeightedVariant(outerSnowyModelRotated),
                        ),
                        Properties.SNOWY,
                    )

                is DirtPathBlock ->
                    createCustomStairsBlockState(
                        stair,
                        createStairsModelMap(
                            createWeightedVariant(innerModel),
                            createWeightedVariant(regularModel),
                            createWeightedVariant(outerModel),
                            createWeightedVariant(innerModelRotated),
                            createWeightedVariant(regularModelRotated),
                            createWeightedVariant(outerModelRotated),
                        ),
                    )

                else ->
                    createStairsBlockState(
                        stair,
                        createWeightedVariant(innerModel),
                        createWeightedVariant(regularModel),
                        createWeightedVariant(outerModel),
                    )
            },
        )
    }

    /**
     * Generates all necessary models and blockstates for a trapdoor variant.
     */
    private fun generateTrapdoorModel(
        trapdoor: Block,
        parent: Block,
    ) {
        val textureMap =
            configureBlockTextureMapping(
                parent = parent,
                hasSideAndTop = parent in hasSideAndTop,
                removeBlock = parent in removeBlock,
                textureKey = "texture",
                top = (if (parent in hasNoTexture) hasNoTexture[parent] else parent)!!,
            )

        val bottomModel =
            Models.TEMPLATE_TRAPDOOR_BOTTOM.upload(
                trapdoor,
                "",
                textureMap,
                generator.modelCollector,
            )

        val topModel =
            Models.TEMPLATE_TRAPDOOR_TOP.upload(
                trapdoor,
                "",
                textureMap,
                generator.modelCollector,
            )

        val openModel =
            Models.TEMPLATE_TRAPDOOR_OPEN.upload(
                trapdoor,
                "",
                textureMap,
                generator.modelCollector,
            )

        generateBlockItemModel(trapdoor, parent, bottomModel, generator)

        generator.blockStateCollector?.accept(
            BlockStateModelGenerator.createTrapdoorBlockState(
                trapdoor,
                createWeightedVariant(topModel),
                createWeightedVariant(bottomModel),
                createWeightedVariant(openModel),
            ),
        )
    }

    /**
     * Generates all necessary models and blockstates for a lantern variant.
     */
    private fun generateLanternModel(
        lantern: Block,
        parent: Block,
    ) {
        // Bestimme die richtige Textur basierend auf Oxidationsstufe
        val textureBlock = when {
            parent == Blocks.COPPER_BLOCK -> {
                val lanternId = Registries.BLOCK.getId(lantern).path
                when {
                    lanternId.contains("oxidized_") -> Blocks.OXIDIZED_COPPER
                    lanternId.contains("weathered_") -> Blocks.WEATHERED_COPPER
                    lanternId.contains("exposed_") -> Blocks.EXPOSED_COPPER
                    else -> Blocks.COPPER_BLOCK
                }
            }
            else -> parent
        }

        val textureMap =
            TextureMap().apply {
                put(TextureKey.TEXTURE, Identifier.of("block/${extractCleanBlockIdentifier(textureBlock)}"))
                put(TextureKey.PARTICLE, Identifier.of(
                    if (parent != Blocks.COPPER_BLOCK) "block/lantern"
                    else {
                        val lanternId = Registries.BLOCK.getId(lantern).path
                        when {
                            lanternId.contains("oxidized_") -> "block/oxidized_copper_lantern"
                            lanternId.contains("weathered_") -> "block/weathered_copper_lantern"
                            lanternId.contains("exposed_") -> "block/exposed_copper_lantern"
                            else -> "block/copper_lantern"
                        }
                    }
                ))
            }
        val standingTemplateModelId =
            when (lantern) {
                is SmallLantern, is SmallRedstoneLantern -> "$MODID:block/template_small_lantern_standing"
                is BigLantern, is BigRedstoneLantern -> "$MODID:block/template_big_lantern_standing"
                else -> "$MODID:block/template_lantern_standing"
            }
        val hangingTemplateModelId =
            when (lantern) {
                is SmallLantern -> "$MODID:block/template_small_lantern_hanging"
                is BigLantern -> "$MODID:block/template_big_lantern_hanging"
                else -> "$MODID:block/template_lantern_hanging"
            }

        val lanternModelStanding =
            Model(
                Optional.of(Identifier.of(standingTemplateModelId)),
                Optional.empty(),
                TextureKey.TEXTURE,
                TextureKey.PARTICLE,
            ).upload(
                lantern,
                "",
                textureMap,
                generator.modelCollector,
            )

        val lanternModelhanging =
            createWeightedVariant(
                Model(
                    Optional.of(Identifier.of(hangingTemplateModelId)),
                    Optional.empty(),
                    TextureKey.TEXTURE,
                    TextureKey.PARTICLE,
                ).upload(
                    lantern,
                    "_hanging",
                    textureMap,
                    generator.modelCollector,
                ),
            )

        if (parent == Blocks.IRON_BLOCK &&
            lantern is RedstoneLantern &&
            lantern !is BigRedstoneLantern &&
            lantern !is SmallRedstoneLantern
        ) {
            val lanternStandingModel = createWeightedVariant(Identifier.ofVanilla("block/lantern"))
            val lanternHangingModel = createWeightedVariant(Identifier.ofVanilla("block/lantern_hanging"))
            val lanternItemModelId = Identifier.ofVanilla("item/lantern")
            generator.registerParentedItemModel(lantern, lanternItemModelId)
            generator.blockStateCollector.accept(
                VariantsBlockModelDefinitionCreator.of(lantern).with(
                    createBooleanModelMap(Properties.HANGING, lanternHangingModel, lanternStandingModel),
                ),
            )
        } else if (parent == Blocks.COPPER_BLOCK &&
            lantern is RedstoneLantern &&
            lantern !is BigRedstoneLantern &&
            lantern !is SmallRedstoneLantern
        ) {
            val lanternStandingModel = createWeightedVariant(Identifier.ofVanilla("block/copper_lantern"))
            val lanternHangingModel = createWeightedVariant(Identifier.ofVanilla("block/copper_lantern_hanging"))
            val lanternItemModelId = Identifier.ofVanilla("item/copper_lantern")
            generator.registerParentedItemModel(lantern, lanternItemModelId)
            generator.blockStateCollector.accept(
                VariantsBlockModelDefinitionCreator.of(lantern).with(
                    createBooleanModelMap(Properties.HANGING, lanternHangingModel, lanternStandingModel),
                ),
            )
        } else {
            generator.blockStateCollector?.accept(
                VariantsBlockModelDefinitionCreator.of(lantern).with(
                    createBooleanModelMap(Properties.HANGING, lanternModelhanging, createWeightedVariant(lanternModelStanding)),
                ),
            )
            generateBlockItemModel(lantern, parent, lanternModelStanding, generator)
        }
    }

    /**
     * Generates all necessary models and blockstates for a slab variant.
     */
    private fun generateSlabModel(
        slab: Block,
        parent: Block,
        top: Block = parent,
        side: Block = parent,
        bottom: Block = parent,
    ) {
        val textureMap =
            configureBlockTextureMapping(
                parent = parent,
                top = top,
                side = side,
                bottom = bottom,
                hasSideAndTop = parent in hasSideAndTop,
                removeBlock = parent in removeBlock,
                bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
                textureKey =
                    if (parent is GrassBlock) {
                        "overlay"
                    } else {
                        ""
                    },
            )

        val snowyTextureMap =
            configureBlockTextureMapping(
                parent = parent,
                top = Blocks.SNOW,
                side = if (parent in snowyOvergrownBlocks) Blocks.GRASS_BLOCK else side,
                bottom = bottom,
                hasSideAndTop = parent in hasSideAndTop,
                removeBlock = parent in removeBlock,
                bottomSameAsTop = parent in listOf<Block>(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
                textureKey = "",
            )

        val bottomModel =
            when (parent) {
                is GrassBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_slab_bottom")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                        TextureKey.LAYER0,
                    ).upload(
                        slab,
                        "",
                        textureMap,
                        generator.modelCollector,
                    )

                is DirtPathBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_path_slab_bottom")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(
                        slab,
                        "",
                        textureMap,
                        generator.modelCollector,
                    )

                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_overgrown_slab_bottom")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(
                        slab,
                        "",
                        textureMap,
                        generator.modelCollector,
                    )

                else ->
                    Models.SLAB.upload(
                        slab,
                        "",
                        textureMap,
                        generator.modelCollector,
                    )
            }

        val snowyBottomModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_snowy_grass_slab_bottom")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(
                        slab,
                        "_snow",
                        snowyTextureMap,
                        generator.modelCollector,
                    )

                else -> null
            }

        val topModel =
            when (parent) {
                is GrassBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_grass_slab_top")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                        TextureKey.LAYER0,
                    ).upload(
                        slab,
                        "_top",
                        textureMap,
                        generator.modelCollector,
                    )

                is DirtPathBlock ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_path_slab_top")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(
                        slab,
                        "_top",
                        textureMap,
                        generator.modelCollector,
                    )

                in snowyOvergrownBlocks ->
                    Model(
                        Optional.of(Identifier.of("$MODID:block/template_overgrown_slab_top")),
                        Optional.empty(),
                        TextureKey.TOP,
                        TextureKey.SIDE,
                        TextureKey.BOTTOM,
                    ).upload(
                        slab,
                        "_top",
                        textureMap,
                        generator.modelCollector,
                    )

                else ->
                    Models.SLAB_TOP.upload(
                        slab,
                        "",
                        textureMap,
                        generator.modelCollector,
                    )
            }

        val snowyTopModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    Models.SLAB_TOP.upload(
                        slab,
                        "_snow_top",
                        snowyTextureMap,
                        generator.modelCollector,
                    )

                else -> null
            }

        val fullBlockModel = Identifier.of("block/${extractCleanBlockIdentifier(parent)}")

        val snowyFullBlockModel =
            when (parent) {
                in snowyOvergrownBlocks -> Identifier.of("block/${extractCleanBlockIdentifier(Blocks.GRASS_BLOCK)}_snow") else -> null
            }

        generateBlockItemModel(slab, parent, bottomModel, generator)

        generator.blockStateCollector?.accept(
            if (parent in snowyOvergrownBlocks) {
                createSnowySlabBlockState(
                    slab,
                    createWeightedVariant(bottomModel),
                    createWeightedVariant(topModel),
                    createWeightedVariant(fullBlockModel),
                    createWeightedVariant(snowyBottomModel),
                    createWeightedVariant(snowyTopModel),
                    createWeightedVariant(snowyFullBlockModel),
                )
            } else {
                createSlabBlockState(
                    slab,
                    createWeightedVariant(bottomModel),
                    createWeightedVariant(topModel),
                    createWeightedVariant(fullBlockModel),
                )
            },
        )
    }
}
