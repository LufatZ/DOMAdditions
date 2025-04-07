@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.BlockSoundGroup
import net.minecraft.sound.BlockSoundGroup.*
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

/**
 * Generates Minecraft block tags for custom blocks based on their characteristics and parent blocks.
 *
 * This provider automatically assigns appropriate mining tool tags (`PICKAXE_MINEABLE`, `AXE_MINEABLE`,
 * `SHOVEL_MINEABLE`, `HOE_MINEABLE`) by inspecting the [BlockSoundGroup] of the parent blocks.
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
    output: FabricDataOutput,
    registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricTagProvider<Block>(output, RegistryKeys.BLOCK, registriesFuture) {
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
                BONE,
                NETHERITE,
                ANCIENT_DEBRIS,
                LODESTONE,
                CHAIN,
                GILDED_BLACKSTONE,
                AMETHYST_BLOCK,
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
                CORAL,
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
                STEM,
                NETHER_STEM,
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
                MOSS_BLOCK,
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
                WEEPING_VINES_LOW_PITCH,
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
                FLOWERBED,
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
                MOSS_BLOCK,
                GRAVEL,
            )

        // Custom BlockTag for DirtlLike Blocks
        val DirtLikeBlockTag: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MODID, "dirt_like"))
        val DirtPathVariantTag: TagKey<Block> = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MODID, "dirt_path_variant"))
    }

    /**
     * Configures and generates the block tags.
     *
     * This method is the main entry point called by the Fabric data generation system.
     * It orchestrates the tagging process for different categories of blocks.
     *
     * @param wrapper The registry wrapper lookup provided by Fabric, used for accessing registry data.
     */
    override fun configure(wrapper: RegistryWrapper.WrapperLookup) {
        // 1. Handle specific block types first
        // Lanterns are always mineable by pickaxe, regardless of parent material sound.
        // This ensures consistency even if the parent sound group might suggest otherwise.
        BlockRegistry.registeredLanterns.keys.forEach { lantern ->
            mineableByPickaxe(lantern)
        }

        // 2. Assign mining tool tags and specific material tags based on parent blocks
        // Process trapdoors using their specific parent list
        assignTagsBasedOnParent(BlockRegistry.registeredTrapdoors, BlockRegistry.trapdoorVariantsParents)
        // Process stairs and slabs using the common block variant parent list
        assignTagsBasedOnParent(BlockRegistry.registeredStairs, BlockRegistry.blockVariantsParents)
        assignTagsBasedOnParent(BlockRegistry.registeredSlabs, BlockRegistry.blockVariantsParents)

        // 3. Add blocks to standard type tags
        BlockRegistry.registeredSlabs.forEach { slab -> addToTag(slab, BlockTags.SLABS) }
        BlockRegistry.registeredStairs.forEach { stair -> addToTag(stair, BlockTags.STAIRS) }
        BlockRegistry.registeredTrapdoors.forEach { trapdoor -> addToTag(trapdoor, BlockTags.TRAPDOORS) }

        // 4. Add blocks to custom tags
        addToTag(Blocks.DIRT_PATH, DirtPathVariantTag)
    }

    /**
     * Assigns mining tool tags and specific material tags to blocks based on their parent block's sound group.
     *
     * Iterates through the provided block list and assigns:
     * 1. A primary mining tool tag ([BlockTags.PICKAXE_MINEABLE], [BlockTags.AXE_MINEABLE], [BlockTags.SHOVEL_MINEABLE], [BlockTags.HOE_MINEABLE])
     * based on the sound group of the corresponding parent block.
     * 2. Additional material-specific tags (e.g., [BlockTags.DIRT]) if the parent's sound group matches predefined sets.
     *
     * If a sound group is [BlockSoundGroup.INTENTIONALLY_EMPTY] or not recognized for tool tagging,
     * a warning is logged, and it defaults to pickaxe-mineable (except for INTENTIONALLY_EMPTY).
     *
     * @param blocks The list of blocks to be tagged (e.g., slabs, stairs).
     * @param parentBlocks The corresponding list of parent blocks. Must be the same size as `blocks`.
     * @param defaultTagAssigner A function reference to assign a default mining tag if the sound group is unhandled. Defaults to [mineableByPickaxe].
     */
    private fun assignTagsBasedOnParent(
        blocks: List<Block>,
        parentBlocks: List<Block>,
        defaultTagAssigner: (Block) -> Unit = ::mineableByPickaxe,
    ) {
        // Basic check to prevent potential IndexOutOfBoundsException
        if (blocks.size != parentBlocks.size) {
            logger.error(
                "[BlockTagGenerator] Block list size (${blocks.size}) and parent block list size (${parentBlocks.size}) mismatch! Skipping tag generation for these lists.",
                IllegalArgumentException("Block and parent list sizes do not match."),
            )
            return
        }

        blocks.forEachIndexed { index, block ->
            // Avoid tagging blocks derived from parents with INTENTIONALLY_EMPTY sound group, log info.
            val parentBlock = parentBlocks[index]
            val parentSoundGroup = parentBlock.defaultState.soundGroup

            if (parentSoundGroup == INTENTIONALLY_EMPTY) {
                logger.info(
                    "[BlockTagGenerator] Skipping tool tag assignment for block [${block.translationKey}] " +
                        "because its parent [${parentBlock.translationKey}] has INTENTIONALLY_EMPTY sound group.",
                )
            } else {
                // Determine and assign the primary mining tool tag
                val toolTagAssigner: ((Block) -> Unit)? =
                    when (parentSoundGroup) {
                        in PICKAXE_SOUND_GROUPS -> ::mineableByPickaxe
                        in AXE_SOUND_GROUPS -> ::mineableByAxe
                        in SHOVEL_SOUND_GROUPS -> ::mineableByShovel
                        in HOE_SOUND_GROUPS -> ::mineableByHoe
                        else -> {
                            logger.warn(
                                """
                                [BlockTagGenerator] Unhandled Sound Group for Mining Tool Tagging:
                                  Block         : ${block.translationKey}
                                  Parent Block  : ${parentBlock.translationKey}
                                  Sound Group   : $parentSoundGroup
                                  Action        : Defaulting to Pickaxe mineable. Consider updating sound group sets or adding specific handling.
                                """.trimIndent(),
                            )
                            defaultTagAssigner // Use the provided default
                        }
                    }
                toolTagAssigner?.invoke(block) // Apply the determined tool tag if one was found/defaulted

                // Assign additional specific material tags based on parent sound group
                if (parentSoundGroup in DIRT_LIKE_SOUND_GROUPS) {
                    val tag = if (parentBlock == Blocks.DIRT_PATH) DirtPathVariantTag else DirtLikeBlockTag
                    addToTag(block, tag)
                    logger.debug(
                        "Added tag [{}] to block [{}] based on parent [{}]'s sound group [{}].",
                        tag.id,
                        block.translationKey,
                        parentBlock.translationKey,
                        parentSoundGroup,
                    )
                }
                // TODO: Add more checks here for other material tags (e.g., PLANKS, LOGS) if needed
            }
        }
    }

    /** Adds the given block to the [BlockTags.PICKAXE_MINEABLE] tag. */
    private fun mineableByPickaxe(block: Block) {
        addToTag(block, BlockTags.PICKAXE_MINEABLE)
    }

    /** Adds the given block to the [BlockTags.AXE_MINEABLE] tag. */
    private fun mineableByAxe(block: Block) {
        addToTag(block, BlockTags.AXE_MINEABLE)
    }

    /** Adds the given block to the [BlockTags.SHOVEL_MINEABLE] tag. */
    private fun mineableByShovel(block: Block) {
        addToTag(block, BlockTags.SHOVEL_MINEABLE)
    }

    /** Adds the given block to the [BlockTags.HOE_MINEABLE] tag. */
    private fun mineableByHoe(block: Block) {
        addToTag(block, BlockTags.HOE_MINEABLE)
    }

    /**
     * Helper method to add a block to a specific tag.
     * Gets or creates the tag builder for the given key and adds the block.
     *
     * @param block The block to add to the tag.
     * @param key The [TagKey] representing the tag to add the block to.
     */
    private fun addToTag(
        block: Block,
        key: TagKey<Block>,
    ) {
        getOrCreateTagBuilder(key).add(block)
    }
}
