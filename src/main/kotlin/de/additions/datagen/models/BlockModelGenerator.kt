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
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.BlockModelGenerators.*
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.ModelTemplates
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.client.resources.model.sprite.Material
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.DirtPathBlock
import net.minecraft.world.level.block.GrassBlock
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import java.util.*

/**
 * Object responsible for generating Minecraft block models for registered blocks within a mod context.
 * Initializes the model generation state and dispatches specific creation tasks for various block types
 * including stairs, slabs, lanterns, trapdoors, and chains based on their parent variants and textures.
 */
object BlockModelGenerator {
    /**
     * The instance responsible for generating block model definitions within the system.
     */
    lateinit var generator: BlockModelGenerators

    /**
     * Initializes the block model generation process by setting the generator instance.
     * Triggers the creation of custom models for registered blocks including stairs, slabs, lanterns, trapdoors, and chains.
     * Handles specific variant behaviors such as bottom material adjustments and copper oxidation states.
     */
    fun BlockModelGenerators.init() {
        generator = this

        generateStairModels()
        generateSlabModels()
        generateLanternModels()
        generateTrapdoorModels()
        generateChainModels()
    }

    /**
     * Generates stair models for all registered stairs based on their associated parent blocks.
     * For specific ground types such as podzol, mycelium, dirt path, and grass block, the generated model explicitly sets the bottom component to dirt; otherwise, it uses the default
     *  configuration.
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
     * Generates block models for all registered slab variants based on their associated parent blocks.
     * Iterates through the registered slabs and retrieves the corresponding parent block configuration.
     * Invokes generateSlabModel with specific bottom texture parameters for podzol, mycelium, dirt path, and grass blocks to ensure correct visual representation.
     * Uses default model generation logic for other parent block types.
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
     * Generates model data for all registered lantern types by iterating through the collection of registered entries.
     * Delegates to generateLanternModel for each entry to handle specific material variations, template selection based on
     * lantern size and parent block properties, as well as registration of standing and hanging variants with appropriate textures.
     */
    private fun generateLanternModels() {
        registeredLanterns.forEach { (lantern, baseBlock) -> generateLanternModel(lantern, baseBlock) }
    }

    /**
     * Generates model files for all registered trapdoor blocks.
     * Iterates through the list of registered trapdoors and retrieves corresponding parent block definitions.
     * For each pair, it delegates the creation of specific models including bottom, top, and open variants along with associated state configurations to a helper function.
     */
    private fun generateTrapdoorModels() {
        registeredTrapdoors.forEachIndexed { index, trapdoor ->
            when (val parentBlock = trapdoorVariantsParents[index]) {
                else -> generateTrapdoorModel(trapdoor, parentBlock)
            }
        }
    }

    /**
     * Generates custom models for all registered chain blocks based on their material composition and oxidation state.
     * Determines specific model identifiers for copper chains depending on whether they are oxidized, weathered, or exposed,
     * while assigning a distinct identifier for iron chains. Creates axis-aligned pillar block custom models and registers corresponding item models via the generator instance.
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
                    }
                else {
                    Identifier.withDefaultNamespace("block/iron_chain")
                }
            val chainItemModelId =
                if (id.contains("copper"))
                    when {
                        id.contains("oxidized_") -> Identifier.withDefaultNamespace("item/oxidized_copper_chain")
                        id.contains("weathered_") -> Identifier.withDefaultNamespace("item/weathered_copper_chain")
                        id.contains("exposed_") -> Identifier.withDefaultNamespace("item/exposed_copper_chain")
                        else -> Identifier.withDefaultNamespace("item/copper_chain")
                    }
                else {
                    Identifier.withDefaultNamespace("item/iron_chain")
                }

            generator.createAxisAlignedPillarBlockCustomModel(chain, plainVariant(chainModelId))
            generator.registerSimpleItemModel(chain, chainItemModelId)
        }
    }

    /**
     * Generates stair model variants for a given block parent. This method configures texture mappings and generates multiple model types including regular, rotated, outer, and inner
     *  stairs based on the parent block type. It handles specific logic for grass blocks, dirt path blocks, and snowy overgrown blocks to determine appropriate textures for top, side
     *  and bottom faces.
     *
     * @param stair The stair block variant being processed.
     * @param parent The base block determining texture mapping rules and model templates.
     * @param top Optional override for the top face texture or block definition. Defaults to the parent.
     * @param side Optional override for the side face texture or block definition. Defaults to the parent.
     * @param bottom Optional override for the bottom face texture or block definition. Defaults to the parent.
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
     * Generates the block state and model variants for a trapdoor block.
     *
     * Configures texture mapping based on the parent block properties, creates separate models
     * for the closed top, closed bottom, and open states, and registers the corresponding item model.
     *
     * @param trapdoor The trapdoor block instance being modeled.
     * @param parent The parent block used to determine texture configuration rules
     *               such as side/top texture presence and removal logic.
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
     * Generates model data and registers block states for lantern blocks based on their type and parent material.
     *
     * Determines texture mappings considering copper oxidation states if the parent is a copper block, selecting specific textures for oxidized, weathered, or exposed variants.
     * Selects appropriate model templates (standing or hanging) depending on whether the lantern is small, big, or standard.
     * Registers specific item models and block states for redstone lanterns placed on iron or copper blocks using simple variant logic.
     * Otherwise, registers generated standing and hanging variants with state-based dispatching for the hanging property.
     *
     * @param lantern The lantern block instance to generate models for, determining template selection logic and oxidation checks.
     * @param parent The parent block type used to determine texture variants and special case handling conditions for redstone lanterns.
     */
    private fun generateLanternModel(
        lantern: Block,
        parent: Block,
    ) {
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

        val particleMaterial = when {
            parent == Blocks.COPPER_BLOCK -> {
                val lanternId = BuiltInRegistries.BLOCK.getKey(lantern).path
                val texturePath = when {
                    lanternId.contains("oxidized_")  -> "block/oxidized_copper_lantern"
                    lanternId.contains("weathered_") -> "block/weathered_copper_lantern"
                    lanternId.contains("exposed_")   -> "block/exposed_copper_lantern"
                    else                             -> "block/copper_lantern"
                }
                Material(Identifier.withDefaultNamespace(texturePath))
            }
            else -> TextureMapping.getBlockTexture(Blocks.LANTERN)
        }

        val textureMap = TextureMapping()
            .put(TextureSlot.TEXTURE, TextureMapping.getBlockTexture(textureBlock))
            .put(TextureSlot.PARTICLE, particleMaterial)
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

        val lanternModelHanging =
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

        when (parent) {
            Blocks.IRON_BLOCK if lantern is RedstoneLantern &&
                    lantern !is BigRedstoneLantern &&
                    lantern !is SmallRedstoneLantern
                -> {
                val lanternStandingModel = plainVariant(Identifier.withDefaultNamespace("block/lantern"))
                val lanternHangingModel = plainVariant(Identifier.withDefaultNamespace("block/lantern_hanging"))
                generator.registerSimpleItemModel(lantern, Identifier.withDefaultNamespace("item/lantern"))
                generator.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(lantern).with(
                        createBooleanModelDispatch(BlockStateProperties.HANGING, lanternHangingModel, lanternStandingModel),
                    ),
                )
            }

            Blocks.COPPER_BLOCK if lantern is RedstoneLantern &&
                    lantern !is BigRedstoneLantern &&
                    lantern !is SmallRedstoneLantern
                -> {
                val lanternStandingModel = plainVariant(Identifier.withDefaultNamespace("block/copper_lantern"))
                val lanternHangingModel = plainVariant(Identifier.withDefaultNamespace("block/copper_lantern_hanging"))
                generator.registerSimpleItemModel(lantern, Identifier.withDefaultNamespace("item/copper_lantern"))
                generator.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(lantern).with(
                        createBooleanModelDispatch(BlockStateProperties.HANGING, lanternHangingModel, lanternStandingModel),
                    ),
                )
            }

            else -> {
                generator.blockStateOutput.accept(
                    MultiVariantGenerator.dispatch(lantern).with(
                        createBooleanModelDispatch(
                            BlockStateProperties.HANGING,
                            lanternModelHanging,
                            plainVariant(lanternModelStanding)
                        ),
                    ),
                )
                generateBlockItemModel(lantern, parent, lanternModelStanding, generator)
            }
        }
    }

    /**
     * Generates model definitions and block states for a slab block variant based on its parent material type.
     * This method configures texture mappings for both standard and snowy conditions, selecting appropriate
     * template models depending on the parent block class such as GrassBlock or DirtPathBlock. It handles
     * specific variations including overgrown blocks and snow cover states before outputting the results.
     * @param slab The specific slab block instance being configured with model definitions.
     * @param parent The base material or block class determining the model template selection logic.
     * @param top The source block for top face textures, defaulting to the parent if not specified.
     * @param side The source block for side face textures, defaulting to the parent if not specified.
     * @param bottom The source block for bottom face textures, defaulting to the parent if not specified.
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
                    ).createWithSuffix(slab, "", textureMap, generator.modelOutput)

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_slab_bottom")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(slab, "", textureMap, generator.modelOutput)

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_slab_bottom")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(slab, "", textureMap, generator.modelOutput)

                else ->
                    ModelTemplates.SLAB_BOTTOM.createWithSuffix(slab, "", textureMap, generator.modelOutput)
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
                    ).createWithSuffix(slab, "_snow", snowyTextureMap, generator.modelOutput)

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
                    ).createWithSuffix(slab, "_top", textureMap, generator.modelOutput)

                is DirtPathBlock ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_path_slab_top")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(slab, "_top", textureMap, generator.modelOutput)

                in snowyOvergrownBlocks ->
                    ModelTemplate(
                        Optional.of(Identifier.parse("$MODID:block/template_overgrown_slab_top")),
                        Optional.empty(),
                        TextureSlot.TOP,
                        TextureSlot.SIDE,
                        TextureSlot.BOTTOM,
                    ).createWithSuffix(slab, "_top", textureMap, generator.modelOutput)

                else ->
                    ModelTemplates.SLAB_TOP.createWithSuffix(slab, "", textureMap, generator.modelOutput)
            }

        val snowyTopModel =
            when (parent) {
                in snowyOvergrownBlocks ->
                    ModelTemplates.SLAB_TOP.createWithSuffix(slab, "_snow_top", snowyTextureMap, generator.modelOutput)

                else -> null
            }

        val fullBlockModel = Identifier.parse("block/${extractCleanBlockIdentifier(parent)}")

        val snowyFullBlockModel =
            when (parent) {
                in snowyOvergrownBlocks -> Identifier.parse("block/${extractCleanBlockIdentifier(Blocks.GRASS_BLOCK)}_snow")
                else -> null
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