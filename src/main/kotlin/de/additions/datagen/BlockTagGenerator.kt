@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry
import de.additions.datagen.BlockTagGenerator.Companion.DirtLikeBlockTag
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.BlockTags
import net.minecraft.tags.TagKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.SoundType.*
import java.util.concurrent.CompletableFuture

/**
 * Generates Minecraft block tags for custom blocks based on their characteristics and parent blocks.
 *
 * This provider automatically assigns appropriate mining tool tags (`MINEABLE_WITH_PICKAXE`, `MINEABLE_WITH_AXE`,
 * `MINEABLE_WITH_SHOVEL`, `MINEABLE_WITH_HOE`) by inspecting the [SoundType] of the parent blocks.
 * It also assigns standard block type tags like `SLABS`, `STAIRS`, `TRAPDOORS`, and specific
 * material tags like `DIRT` based on parent properties.
 *
 * It handles specific logic for lanterns (always pickaxe-mineable) and derives tags for trapdoors,
 * stairs, and slabs from their corresponding parent blocks.
 *
 * Note: The approach uses the parent block's sound group as a proxy for its material type.
 * Sound groups without a clear tool mapping default to pickaxe-mineable with a warning.
 * Blocks needing specific tools (e.g., Shears for Wool/Cobweb) are not explicitly handled here.
 *
 * @property output The Fabric data output instance.
 * @property registriesFuture A future providing asynchronous access to the registry wrapper lookup.
 */
class BlockTagGenerator(
    output: FabricPackOutput,
    registriesFuture: CompletableFuture<HolderLookup.Provider>,
) : FabricTagsProvider<Block>(output, Registries.BLOCK, registriesFuture) {

    /** Shorthand to resolve a [Block] instance to its [ResourceKey] for use with [builder]. */
    private fun Block.key(): ResourceKey<Block> = BuiltInRegistries.BLOCK.getResourceKey(this).orElseThrow()

    companion object {
        // Groups typically mined fastest with a Pickaxe
        private val PICKAXE_SOUND_GROUPS =
            setOf(
                STONE,
                METAL,
                ANVIL,
                NETHERRACK,
                NETHER_BRICKS,
                NETHER_ORE,
                NETHER_GOLD_ORE,
                BONE_BLOCK,
                NETHERITE_BLOCK,
                ANCIENT_DEBRIS,
                LODESTONE,
                CHAIN,
                GILDED_BLACKSTONE,
                AMETHYST,
                AMETHYST_CLUSTER,
                SMALL_AMETHYST_BUD,
                MEDIUM_AMETHYST_BUD,
                LARGE_AMETHYST_BUD,
                TUFF,
                TUFF_BRICKS,
                POLISHED_TUFF,
                CALCITE,
                DRIPSTONE_BLOCK,
                POINTED_DRIPSTONE,
                COPPER,
                COPPER_BULB,
                COPPER_GRATE,
                DEEPSLATE,
                DEEPSLATE_BRICKS,
                DEEPSLATE_TILES,
                POLISHED_DEEPSLATE,
                BASALT,
                SHROOMLIGHT,
                FROGLIGHT,
                CORAL_BLOCK,
                TRIAL_SPAWNER,
                SPAWNER,
                VAULT,
                HEAVY_CORE,
                LANTERN,
                DECORATED_POT,
                IRON,
            )

        // Groups typically mined fastest with an Axe
        private val AXE_SOUND_GROUPS =
            setOf(
                WOOD,
                NETHER_WOOD,
                CHERRY_WOOD,
                BAMBOO_WOOD,
                HARD_CROP,
                STEM,
                MANGROVE_ROOTS,
                HANGING_SIGN,
                NETHER_WOOD_HANGING_SIGN,
                BAMBOO_WOOD_HANGING_SIGN,
                CHERRY_WOOD_HANGING_SIGN,
                CHISELED_BOOKSHELF,
                BAMBOO,
                WART_BLOCK,
            )

        // Groups typically mined fastest with a Shovel
        private val SHOVEL_SOUND_GROUPS =
            setOf(
                GRAVEL,
                SAND,
                SUSPICIOUS_SAND,
                SUSPICIOUS_GRAVEL,
                GRASS,
                WET_GRASS,
                SOUL_SAND,
                SOUL_SOIL,
                ROOTED_DIRT,
                MUD,
                MUD_BRICKS,
                PACKED_MUD,
                MUDDY_MANGROVE_ROOTS,
                SNOW,
                POWDER_SNOW,
                NYLIUM,
            )

        // Groups typically mined fastest/correctly with a Hoe
        private val HOE_SOUND_GROUPS =
            setOf(
                MOSS,
                MOSS_CARPET,
                AZALEA_LEAVES,
                CHERRY_LEAVES,
                SCULK,
                SCULK_SENSOR,
                SCULK_CATALYST,
                SCULK_VEIN,
                SCULK_SHRIEKER,
                SPONGE,
                WET_SPONGE,
                SHROOMLIGHT,
                NETHER_WART,
                WART_BLOCK,
                VINE,
                GLOW_LICHEN,
                WEEPING_VINES,
                TWISTING_VINES,
                CAVE_VINES,
                LILY_PAD,
                BIG_DRIPLEAF,
                SMALL_DRIPLEAF,
                HANGING_ROOTS,
                ROOTS,
                NETHER_SPROUTS,
                FUNGUS,
                CROP,
                SWEET_BERRY_BUSH,
                AZALEA,
                FLOWERING_AZALEA,
                PINK_PETALS,
                SPORE_BLOSSOM,
                BAMBOO_SAPLING,
                CHERRY_SAPLING,
                LEAF_LITTER,
            )

        // Specific material groups for assigning extra tags (e.g., BlockTags.DIRT)
        private val DIRT_LIKE_SOUND_GROUPS =
            setOf(
                GRASS,
                ROOTED_DIRT,
                MUD,
                MUDDY_MANGROVE_ROOTS,
                NYLIUM,
                MOSS,
                GRAVEL,
            )

        // Custom BlockTags
        val DirtLikeBlockTag: TagKey<Block> = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MODID, "dirt_like"))
        val DirtPathVariantTag: TagKey<Block> = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MODID, "dirt_path_variant"))

        /**
         * Custom BlockTag bundling all vanilla ore tags and serving as the primary ore detection
         * mechanism for vein mining. Other mods can extend this tag to add their own ores by
         * including `additions:ores` in their own tag files.
         * Used in [VeinMineItem] as the first check before falling back to naming convention.
         */
        val OresTag: TagKey<Block> = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MODID, "ores"))
    }

    /**
     * Configures and generates the block tags.
     *
     * This method is the main entry point called by the Fabric data generation system.
     * It orchestrates the tagging process for different categories of blocks.
     *
     * @param wrapper The registry wrapper lookup provided by Fabric, used for accessing registry data.
     */
    override fun addTags(wrapper: HolderLookup.Provider) {
        // 1. Handle specific block types first.
        // Lanterns are always mineable by pickaxe, regardless of parent material sound.
        BlockRegistry.registeredLanterns.keys.forEach { lantern ->
            builder(BlockTags.MINEABLE_WITH_PICKAXE).add(lantern.key())
        }

        // 2. Assign mining tool tags and specific material tags based on parent blocks.
        assignTagsBasedOnParent(BlockRegistry.registeredTrapdoors, BlockRegistry.trapdoorVariantsParents)
        assignTagsBasedOnParent(BlockRegistry.registeredStairs, BlockRegistry.blockVariantsParents)
        assignTagsBasedOnParent(BlockRegistry.registeredSlabs, BlockRegistry.blockVariantsParents)

        // 3. Add blocks to standard type tags.
        builder(BlockTags.SLABS).addAll(BlockRegistry.registeredSlabs.map { it.key() })
        builder(BlockTags.STAIRS).addAll(BlockRegistry.registeredStairs.map { it.key() })
        builder(BlockTags.TRAPDOORS).addAll(BlockRegistry.registeredTrapdoors.map { it.key() })

        // 4. Add blocks to custom tags.
        builder(DirtPathVariantTag).add(Blocks.DIRT_PATH.key())
        builder(BlockTags.ENABLES_BUBBLE_COLUMN_DRAG_DOWN).addAll(BlockRegistry.getRegisteredMagmaBlocks().map { it.key() })

        // 5. Build the additions:ores tag from all vanilla ore tags.
        // Vanilla tags are always present, added directly via addTag.
        // Mod-provided ore tags (e.g. from Create, Thermal etc.) are included as optional tags
        // so the build does not fail if those mods are not present.
        // Other mods can extend additions:ores by adding it as a parent in their own tag files.
        builder(OresTag)
            .addOptionalTag(BlockTags.COAL_ORES)
            .addOptionalTag(BlockTags.COPPER_ORES)
            .addOptionalTag(BlockTags.DIAMOND_ORES)
            .addOptionalTag(BlockTags.EMERALD_ORES)
            .addOptionalTag(BlockTags.GOLD_ORES)
            .addOptionalTag(BlockTags.IRON_ORES)
            .addOptionalTag(BlockTags.LAPIS_ORES)
            .addOptionalTag(BlockTags.REDSTONE_ORES)
            // Common cross-mod ore convention tags (c: namespace, used by many Fabric mods).
            // Added as optional so the build does not fail when the mod is absent.
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/zinc")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/silver")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/lead")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/tin")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/nickel")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/uranium")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/aluminum")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/osmium")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/fluorite")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/apatite")))
            .addOptionalTag(TagKey.create(Registries.BLOCK, Identifier.parse("c:ores/sulfur")))
    }

    /**
     * Assigns mining tool tags and specific material tags to blocks based on their parent block's sound group.
     *
     * Iterates through the provided block list and assigns:
     * 1. A primary mining tool tag ([BlockTags.MINEABLE_WITH_PICKAXE], [BlockTags.MINEABLE_WITH_AXE],
     * [BlockTags.MINEABLE_WITH_SHOVEL], [BlockTags.MINEABLE_WITH_HOE]) based on the sound group
     * of the corresponding parent block.
     * 2. Additional material-specific tags (e.g., [DirtLikeBlockTag]) if the parent's sound group
     * matches predefined sets.
     *
     * If a sound group is [EMPTY], the block is skipped with an info log.
     * Unrecognized sound groups default to [defaultTag] with a warning.
     *
     * @param blocks The list of blocks to be tagged (e.g., slabs, stairs).
     * @param parentBlocks The corresponding list of parent blocks. Must be the same size as [blocks].
     * @param defaultTag The mining tool tag to fall back to if the sound group is unrecognized.
     * Defaults to [BlockTags.MINEABLE_WITH_PICKAXE].
     */
    private fun assignTagsBasedOnParent(
        blocks: List<Block>,
        parentBlocks: List<Block>,
        defaultTag: TagKey<Block> = BlockTags.MINEABLE_WITH_PICKAXE,
    ) {
        if (blocks.size != parentBlocks.size) {
            logger.error(
                "[BlockTagGenerator] Block list size (${blocks.size}) and parent block list size " +
                "(${parentBlocks.size}) mismatch! Skipping tag generation for these lists.",
                IllegalArgumentException("Block and parent list sizes do not match."),
            )
            return
        }

        blocks.forEachIndexed { index, block ->
            val parentBlock = parentBlocks[index]
            val parentSoundGroup = parentBlock.defaultBlockState().soundType

            if (parentSoundGroup == EMPTY) {
                logger.info(
                    "[BlockTagGenerator] Skipping tool tag assignment for block [${block.descriptionId}] " +
                        "because its parent [${parentBlock.descriptionId}] has INTENTIONALLY_EMPTY sound group.",
                )
                return@forEachIndexed
            }

            // Determine the primary mining tool tag from the parent's sound group.
            val toolTag: TagKey<Block> = when (parentSoundGroup) {
                in PICKAXE_SOUND_GROUPS -> BlockTags.MINEABLE_WITH_PICKAXE
                in AXE_SOUND_GROUPS -> BlockTags.MINEABLE_WITH_AXE
                in SHOVEL_SOUND_GROUPS -> BlockTags.MINEABLE_WITH_SHOVEL
                in HOE_SOUND_GROUPS -> BlockTags.MINEABLE_WITH_HOE
                else -> {
                    logger.warn(
                        """
                        [BlockTagGenerator] Unhandled Sound Group for Mining Tool Tagging:
                          Block         : ${block.descriptionId}
                          Parent Block  : ${parentBlock.descriptionId}
                          Sound Group   : $parentSoundGroup
                          Action        : Defaulting to ${defaultTag.location()}. Consider updating sound group sets or adding specific handling.
                        """.trimIndent(),
                    )
                    defaultTag
                }
            }
            builder(toolTag).add(block.key())

            // Assign additional dirt-like material tag if applicable.
            if (parentSoundGroup in DIRT_LIKE_SOUND_GROUPS) {
                val tag = if (parentBlock == Blocks.DIRT_PATH) DirtPathVariantTag else DirtLikeBlockTag
                builder(tag).add(block.key())
                logger.debug(
                    "Added tag [{}] to block [{}] based on parent [{}]'s sound group [{}].",
                    tag.location,
                    block.descriptionId,
                    parentBlock.descriptionId,
                    parentSoundGroup,
                )
            }
            // TODO: Add more checks here for other material tags (e.g., PLANKS, LOGS) if needed
        }
    }
}