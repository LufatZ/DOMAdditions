package de.additions.datagen.models

import de.additions.Additions.MODID
import de.additions.blocks.BlockRegistry.blockVariantsParents
import de.additions.blocks.BlockRegistry.registeredChains
import de.additions.blocks.BlockRegistry.registeredLanterns
import de.additions.blocks.BlockRegistry.registeredSlabs
import de.additions.blocks.BlockRegistry.registeredStairs
import de.additions.blocks.BlockRegistry.registeredTrapdoors
import de.additions.blocks.BlockRegistry.trapdoorVariantsParents
import de.additions.datagen.models.CustomStates.createOvergrownStairsBlockState
import de.additions.datagen.models.CustomStates.createSnowySlabBlockState
import de.additions.datagen.models.CustomStates.createSnowyStairsBlockState
import de.additions.datagen.models.ModelGenerator.Companion.hasNoTexture
import de.additions.datagen.models.ModelGenerator.Companion.hasSideAndTop
import de.additions.datagen.models.ModelGenerator.Companion.removeBlock
import de.additions.datagen.models.ModelGenerator.Companion.snowyOvergrownBlocks
import de.additions.datagen.models.ModelHelper.configureBlockTextureMapping
import de.additions.datagen.models.ModelHelper.extractCleanBlockIdentifier
import de.additions.datagen.models.ModelHelper.generateBlockItemModel
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.block.DirtPathBlock
import net.minecraft.block.GrassBlock
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.BlockStateModelGenerator.createBooleanModelMap
import net.minecraft.client.data.BlockStateModelGenerator.createStairsBlockState
import net.minecraft.client.data.Model
import net.minecraft.client.data.Models
import net.minecraft.client.data.TextureKey
import net.minecraft.client.data.TextureMap
import net.minecraft.client.data.VariantsBlockStateSupplier
import net.minecraft.state.property.Properties
import net.minecraft.util.Identifier
import java.util.Optional

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
        val chainModelId = Identifier.ofVanilla("block/chain")
        val chainItemModelId = Identifier.ofVanilla("item/chain")

        registeredChains.forEach { chain ->
            generator.registerAxisRotated(chain, chainModelId)
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
        val defaultModel =
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

        val defaultModelRotated =
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
        val snowyDefaultModel =
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

        val snowyDefaultModelRotated =
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

        val snowyOuterModel =
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

        val snowyOuterModelRotated =
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

        val snowyInnerModel =
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

        val snowyInnerModelRotated =
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

        generateBlockItemModel(stair, parent, defaultModel, generator)

        generator.blockStateCollector?.accept(
            when (parent) {
                in snowyOvergrownBlocks ->
                    createSnowyStairsBlockState(
                        stair,
                        innerModel,
                        defaultModel,
                        outerModel,
                        innerModelRotated,
                        defaultModelRotated,
                        outerModelRotated,
                        snowyInnerModel,
                        snowyDefaultModel,
                        snowyOuterModel,
                        snowyInnerModelRotated,
                        snowyDefaultModelRotated,
                        snowyOuterModelRotated,
                    )

                is DirtPathBlock ->
                    createOvergrownStairsBlockState(
                        stair,
                        innerModel,
                        defaultModel,
                        outerModel,
                        innerModelRotated,
                        defaultModelRotated,
                        outerModelRotated,
                    )

                else ->
                    createStairsBlockState(
                        stair,
                        innerModel,
                        defaultModel,
                        outerModel,
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
                topModel,
                bottomModel,
                openModel,
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
        val textureMap =
            TextureMap().apply {
                put(TextureKey.TEXTURE, Identifier.of("block/${extractCleanBlockIdentifier(parent)}"))
                put(TextureKey.PARTICLE, Identifier.of("block/lantern"))
            }

        val lanternModelStanding =
            Model(
                Optional.of(Identifier.of("$MODID:block/template_lantern_standing")),
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
            Model(
                Optional.of(Identifier.of("$MODID:block/template_lantern_hanging")),
                Optional.empty(),
                TextureKey.TEXTURE,
                TextureKey.PARTICLE,
            ).upload(
                lantern,
                "_hanging",
                textureMap,
                generator.modelCollector,
            )

        if (parent == Blocks.IRON_BLOCK) {
            val lanternStandingModelId = Identifier.ofVanilla("block/lantern")
            val lanternHangingModelId = Identifier.ofVanilla("block/lantern_hanging")
            val lanternItemModelId = Identifier.ofVanilla("item/lantern")
            generator.registerParentedItemModel(lantern, lanternItemModelId)
            generator.blockStateCollector.accept(
                VariantsBlockStateSupplier.create(lantern).coordinate(
                    createBooleanModelMap(Properties.HANGING, lanternHangingModelId, lanternStandingModelId),
                ),
            )
        } else {
            generator.blockStateCollector?.accept(
                VariantsBlockStateSupplier
                    .create(lantern)
                    .coordinate(createBooleanModelMap(Properties.HANGING, lanternModelhanging, lanternModelStanding)),
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
                    bottomModel,
                    topModel,
                    fullBlockModel,
                    snowyBottomModel,
                    snowyTopModel,
                    snowyFullBlockModel,
                )
            } else {
                BlockStateModelGenerator.createSlabBlockState(
                    slab,
                    bottomModel,
                    topModel,
                    fullBlockModel,
                )
            },
        )
    }
}
