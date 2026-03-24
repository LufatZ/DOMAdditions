@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen.models

import de.additions.Additions.MODID
import de.additions.blocks.BlockRegistry.blockVariantsParents
import de.additions.blocks.BlockRegistry.registeredChains
import de.additions.blocks.BlockRegistry.registeredLanterns
import de.additions.blocks.BlockRegistry.registeredSlabs
import de.additions.blocks.BlockRegistry.registeredStairs
import de.additions.blocks.BlockRegistry.registeredTrapdoors
import de.additions.blocks.BlockRegistry.trapdoorVariantsParents
import de.additions.blocks.lanterns.*
import de.additions.datagen.ModelGenerator.Companion.hasNoTexture
import de.additions.datagen.ModelGenerator.Companion.hasSideAndTop
import de.additions.datagen.ModelGenerator.Companion.removeBlock
import de.additions.datagen.ModelGenerator.Companion.snowyOvergrownBlocks
import de.additions.datagen.models.CustomStates.createCustomStairsBlockState
import de.additions.datagen.models.CustomStates.createSnowySlabBlockState
import de.additions.datagen.models.CustomStates.createStairsModelMap
import de.additions.helper.ModelHelper.configureBlockTextureMapping
import de.additions.helper.ModelHelper.extractCleanBlockIdentifier
import de.additions.helper.ModelHelper.generateBlockItemModel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.DirtPathBlock
import net.minecraft.world.level.block.GrassBlock
import net.minecraft.client.data.*
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.BlockModelGenerators.*
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.resources.Identifier
import java.util.*

object BlockModelGenerator {
    lateinit var generator: BlockModelGenerators

    fun BlockModelGenerators.init() {
        generator = this

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
            val id = BuiltInRegistries.BLOCK.getKey(chain).path
            val chainModelId =
                if (id.contains("copper"))
                    when {
                        id.contains("oxidized_") -> Identifier.withDefaultNamespace("block/oxidized_copper_chain")
                        id.contains("weathered_") -> Identifier.withDefaultNamespace("block/weathered_copper_chain")
                        id.contains("exposed_") -> Identifier.withDefaultNamespace("block/exposed_copper_chain")
                        else -> Identifier.withDefaultNamespace("block/copper_chain")
                    } else {
                    Identifier.withDefaultNamespace("block/iron_chain")
                }
            val chainItemModelId =
                if (id.contains("copper"))
                    when {
                        id.contains("oxidized_") -> Identifier.withDefaultNamespace("item/oxidized_copper_chain")
                        id.contains("weathered_") -> Identifier.withDefaultNamespace("item/weathered_copper_chain")
                        id.contains("exposed_") -> Identifier.withDefaultNamespace("item/exposed_copper_chain")
                        else -> Identifier.withDefaultNamespace("item/copper_chain")
                    } else {
                    Identifier.withDefaultNamespace("item/iron_chain")
                }


            generator.createAxisAlignedPillarBlockCustomModel(chain, plainVariant(chainModelId))
            generator.registerSimpleItemModel(chain, chainItemModelId)
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
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                        TextureSlot.LAYER0,
                    ).createWithSuffix(stair, "", textureMap, generator.modelOutput)

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_stair")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "", textureMap, generator.modelOutput)

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_stair")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "", textureMap, generator.modelOutput)

                else -> ModelTemplates.STAIRS_STRAIGHT.createWithSuffix(stair, "", textureMap, generator.modelOutput)
            }

        val regularModelRotated =
            when (parent) {
                is GrassBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                        TextureSlot.LAYER0,
                    ).createWithSuffix(stair, "_rotated", textureMap, generator.modelOutput)

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_stair_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_rotated", textureMap, generator.modelOutput)

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_stair_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_rotated", textureMap, generator.modelOutput)

                else -> ModelTemplates.STAIRS_STRAIGHT.createWithSuffix(stair, "_rotated", textureMap, generator.modelOutput)
            }

        val outerModel =
            when (parent) {
                is GrassBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_outer")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                        TextureSlot.LAYER0,
                    ).createWithSuffix(stair, "_outer", textureMap, generator.modelOutput)

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_stair_outer")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_outer", textureMap, generator.modelOutput)

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_stair_outer")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_outer", textureMap, generator.modelOutput)

                else -> ModelTemplates.STAIRS_OUTER.createWithSuffix(stair, "", textureMap, generator.modelOutput)
            }

        val outerModelRotated =
            when (parent) {
                is GrassBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_outer_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                        TextureSlot.LAYER0,
                    ).createWithSuffix(stair, "_outer_rotated", textureMap, generator.modelOutput)

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_stair_outer_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_outer_rotated", textureMap, generator.modelOutput)

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_stair_outer_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_outer_rotated", textureMap, generator.modelOutput)

                else -> ModelTemplates.STAIRS_OUTER.createWithSuffix(stair, "_rotated", textureMap, generator.modelOutput)
            }

        val innerModel =
            when (parent) {
                is GrassBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_inner")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                        TextureSlot.LAYER0,
                    ).createWithSuffix(stair, "_inner", textureMap, generator.modelOutput)

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_stair_inner")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_inner", textureMap, generator.modelOutput)

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_stair_inner")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_inner", textureMap, generator.modelOutput)

                else -> ModelTemplates.STAIRS_INNER.createWithSuffix(stair, "", textureMap, generator.modelOutput)
            }

        val innerModelRotated =
            when (parent) {
                is GrassBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_inner_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                        TextureSlot.LAYER0,
                    ).createWithSuffix(stair, "_inner_rotated", textureMap, generator.modelOutput)

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_stair_inner_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_inner_rotated", textureMap, generator.modelOutput)

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_stair_inner_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_inner_rotated", textureMap, generator.modelOutput)

                else -> ModelTemplates.STAIRS_INNER.createWithSuffix(stair, "_rotated", textureMap, generator.modelOutput)
            }

        // Snowy models for grass blocks
        val regularSnowyModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_snowy")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_snowy", snowyTextureMap, generator.modelOutput)

                else -> null
            }

        val regularSnowyModelRotated =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_snowy_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_snowy_rotated", snowyTextureMap, generator.modelOutput)

                else -> null
            }

        val outerSnowyModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_outer_snowy")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_snowy_outer", snowyTextureMap, generator.modelOutput)

                else -> null
            }

        val outerSnowyModelRotated =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_outer_snowy_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_snowy_outer_rotated", snowyTextureMap, generator.modelOutput)

                else -> null
            }

        val innerSnowyModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_inner_snowy")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_snowy_inner", snowyTextureMap, generator.modelOutput)

                else -> null
            }

        val innerSnowyModelRotated =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_stair_inner_snowy_rotated")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(stair, "_snowy_inner_rotated", snowyTextureMap, generator.modelOutput)

                else -> null
            }

        generateBlockItemModel(stair, parent, regularModel, generator)

        generator.blockStateOutput.accept(
            when (parent) {
                in snowyOvergrownBlocks ->
                    createCustomStairsBlockState(
                        stair,
                        createStairsModelMap(
                            plainVariant(innerModel),
                            plainVariant(regularModel),
                            plainVariant(outerModel),
                            plainVariant(innerModelRotated),
                            plainVariant(regularModelRotated),
                            plainVariant(outerModelRotated),
                            plainVariant(innerSnowyModel!!),
                            plainVariant(regularSnowyModel!!),
                            plainVariant(outerSnowyModel!!),
                            plainVariant(innerSnowyModelRotated!!),
                            plainVariant(regularSnowyModelRotated!!),
                            plainVariant(outerSnowyModelRotated!!),
                        ),
                        BlockStateProperties.SNOWY,
                    )

                is DirtPathBlock ->
                    createCustomStairsBlockState(
                        stair,
                        createStairsModelMap(
                            plainVariant(innerModel),
                            plainVariant(regularModel),
                            plainVariant(outerModel),
                            plainVariant(innerModelRotated),
                            plainVariant(regularModelRotated),
                            plainVariant(outerModelRotated),
                        ),
                    )

                else ->
                    createStairs(
                        stair,
                        plainVariant(innerModel),
                        plainVariant(regularModel),
                        plainVariant(outerModel),
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
            ModelTemplates.TRAPDOOR_BOTTOM.createWithSuffix(
                trapdoor,
                "",
                textureMap,
                generator.modelOutput,
            )

        val topModel =
            ModelTemplates.TRAPDOOR_TOP.createWithSuffix(
                trapdoor,
                "",
                textureMap,
                generator.modelOutput,
            )

        val openModel =
            ModelTemplates.TRAPDOOR_OPEN.createWithSuffix(
                trapdoor,
                "",
                textureMap,
                generator.modelOutput,
            )

        generateBlockItemModel(trapdoor, parent, bottomModel, generator)

        generator.blockStateOutput.accept(
            BlockModelGenerators.createTrapdoor(
                trapdoor,
                plainVariant(topModel),
                plainVariant(bottomModel),
                plainVariant(openModel),
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
                val lanternId = BuiltInRegistries.BLOCK.getKey(lantern).path
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
            TextureMapping().apply {
                put(TextureSlot.TEXTURE, Identifier.parse("block/${extractCleanBlockIdentifier(textureBlock)}"))
                put(
                    TextureSlot.PARTICLE, Identifier.parse(
                    if (parent != Blocks.COPPER_BLOCK) "block/lantern"
                    else {
                        val lanternId = BuiltInRegistries.BLOCK.getKey(lantern).path
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
            ModelTemplate(
                Optional.of(Identifier.parse(standingTemplateModelId)),
                Optional.empty(),
                TextureSlot.TEXTURE,
                TextureSlot.PARTICLE,
            ).createWithSuffix(
                lantern,
                "",
                textureMap,
                generator.modelOutput,
            )

        val lanternModelhanging =
            plainVariant(
                ModelTemplate(
                    Optional.of(Identifier.parse(hangingTemplateModelId)),
                    Optional.empty(),
                    TextureSlot.TEXTURE,
                    TextureSlot.PARTICLE,
                ).createWithSuffix(
                    lantern,
                    "_hanging",
                    textureMap,
                    generator.modelOutput,
                ),
            )

        if (parent == Blocks.IRON_BLOCK &&
            lantern is RedstoneLantern &&
            lantern !is BigRedstoneLantern &&
            lantern !is SmallRedstoneLantern
        ) {
            val lanternStandingModel = plainVariant(Identifier.withDefaultNamespace("block/lantern"))
            val lanternHangingModel = plainVariant(Identifier.withDefaultNamespace("block/lantern_hanging"))
            val lanternItemModelId = Identifier.withDefaultNamespace("item/lantern")
            generator.registerSimpleItemModel(lantern, lanternItemModelId)
            generator.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(lantern).with(
                    createBooleanModelDispatch(BlockStateProperties.HANGING, lanternHangingModel, lanternStandingModel),
                ),
            )
        } else if (parent == Blocks.COPPER_BLOCK &&
            lantern is RedstoneLantern &&
            lantern !is BigRedstoneLantern &&
            lantern !is SmallRedstoneLantern
        ) {
            val lanternStandingModel = plainVariant(Identifier.withDefaultNamespace("block/copper_lantern"))
            val lanternHangingModel = plainVariant(Identifier.withDefaultNamespace("block/copper_lantern_hanging"))
            val lanternItemModelId = Identifier.withDefaultNamespace("item/copper_lantern")
            generator.registerSimpleItemModel(lantern, lanternItemModelId)
            generator.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(lantern).with(
                    createBooleanModelDispatch(BlockStateProperties.HANGING, lanternHangingModel, lanternStandingModel),
                ),
            )
        } else {
            generator.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(lantern).with(
                    createBooleanModelDispatch(BlockStateProperties.HANGING, lanternModelhanging, plainVariant(lanternModelStanding)),
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
                bottomSameAsTop = parent in listOf(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
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
                bottomSameAsTop = parent in listOf(Blocks.MUDDY_MANGROVE_ROOTS, Blocks.POLISHED_BASALT),
                textureKey = "",
            )

        val bottomModel =
            when (parent) {
                is GrassBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_slab_bottom")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                        TextureSlot.LAYER0,
                    ).createWithSuffix(
                        slab,
                        "",
                        textureMap,
                        generator.modelOutput,
                    )

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_slab_bottom")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(
                        slab,
                        "",
                        textureMap,
                        generator.modelOutput,
                    )

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_slab_bottom")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(
                        slab,
                        "",
                        textureMap,
                        generator.modelOutput,
                    )

                else ->
                    ModelTemplates.SLAB_BOTTOM.createWithSuffix(
                        slab,
                        "",
                        textureMap,
                        generator.modelOutput,
                    )
            }

        val snowyBottomModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_snowy_grass_slab_bottom")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(
                        slab,
                        "_snow",
                        snowyTextureMap,
                        generator.modelOutput,
                    )

                else -> null
            }

        val topModel =
            when (parent) {
                is GrassBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_grass_slab_top")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                        TextureSlot.LAYER0,
                    ).createWithSuffix(
                        slab,
                        "_top",
                        textureMap,
                        generator.modelOutput,
                    )

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_slab_top")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(
                        slab,
                        "_top",
                        textureMap,
                        generator.modelOutput,
                    )

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_slab_top")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(
                        slab,
                        "_top",
                        textureMap,
                        generator.modelOutput,
                    )

                else ->
                    ModelTemplates.SLAB_TOP.createWithSuffix(
                        slab,
                        "",
                        textureMap,
                        generator.modelOutput,
                    )
            }

        val snowyTopModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplates.SLAB_TOP.createWithSuffix(
                        slab,
                        "_snow_top",
                        snowyTextureMap,
                        generator.modelOutput,
                    )

                else -> null
            }

        val fullBlockModel = Identifier.parse("block/${extractCleanBlockIdentifier(parent)}")

        val snowyFullBlockModel =
            when (parent) {
                in snowyOvergrownBlocks -> Identifier.parse("block/${extractCleanBlockIdentifier(Blocks.GRASS_BLOCK)}_snow") else -> null
            }

        generateBlockItemModel(slab, parent, bottomModel, generator)

        generator.blockStateOutput.accept(
            if (parent in snowyOvergrownBlocks) {
                createSnowySlabBlockState(
                    slab,
                    plainVariant(bottomModel),
                    plainVariant(topModel),
                    plainVariant(fullBlockModel),
                    plainVariant(snowyBottomModel!!),
                    plainVariant(snowyTopModel!!),
                    plainVariant(snowyFullBlockModel!!),
                )
            } else {
                createSlab(
                    slab,
                    plainVariant(bottomModel),
                    plainVariant(topModel),
                    plainVariant(fullBlockModel),
                )
            },
        )
    }
}
