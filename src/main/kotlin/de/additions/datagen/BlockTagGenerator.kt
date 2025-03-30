package de.additions.datagen

import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.BlockSoundGroup
import java.util.concurrent.CompletableFuture

/**
 * Generates block tags for mining based on block characteristics and variants.
 *
 * This generator intelligently assigns appropriate mining tags to different block types
 * by analyzing their parent blocks and sound characteristics. It supports automatic
 * tag generation for lanterns, trapdoors, stairs, and slabs.
 *
 * @property generator The Fabric data output generator
 * @property registryLookup Asynchronous registry wrapper for block lookups
 *
 * @see BlockRegistry
 * @see BlockTags
 */
class BlockTagGenerator(
    generator: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricTagProvider<Block>(generator, RegistryKeys.BLOCK, registryLookup) {
    /**
     * Configures block tags for different block variants.
     *
     * This method handles tag generation for:
     * - Lanterns (always pickaxe-mineable)
     * - Trapdoors (based on parent block type)
     * - Stairs and slabs (based on parent block type)
     *
     * Note: Originally, there was an intention to directly use the original block tags
     * of the parent blocks. However, due to implementation challenges, a custom
     * approach using sound group characteristics was developed instead.
     *
     * @param wrapper Registry wrapper providing block lookup capabilities
     */
    override fun configure(wrapper: RegistryWrapper.WrapperLookup) {
        // Lanterns are always mineable by pickaxe
        BlockRegistry.registeredLanterns.keys
            .toList()
            .forEach { lantern -> mineableByPickaxe(lantern) }

        // inherit mining tags from their parent blocks
        useParentMineable(BlockRegistry.registeredTrapdoors, BlockRegistry.trapdoorVariantsParents)
        useParentMineable(BlockRegistry.registeredStairs, BlockRegistry.blockVariantsParents)
        useParentMineable(BlockRegistry.registeredSlabs, BlockRegistry.blockVariantsParents)
        // add all slabs to slab tag
        BlockRegistry.registeredSlabs.forEach { slab -> addToTag(slab, BlockTags.SLABS) }
        BlockRegistry.registeredStairs.forEach { stair -> addToTag(stair, BlockTags.STAIRS) }
        BlockRegistry.registeredTrapdoors.forEach { trapdoor -> addToTag(trapdoor, BlockTags.TRAPDOORS) }
    }

    /**
     * Assigns mining tags to blocks based on their parent block's sound characteristics.
     *
     * This method provides intelligent tag generation by:
     * - Matching each block with its corresponding parent block
     * - Determining the appropriate mining tool based on the parent's sound group
     *
     * @param blockList List of blocks to be tagged
     * @param parentList Corresponding list of parent blocks
     * @param defaultTag Fallback tag generation method (defaults to pickaxe)
     */
    private fun useParentMineable(
        blockList: List<Block>,
        parentList: List<Block>,
        defaultTag: (Block) -> Unit = ::mineableByPickaxe,
    ) {
        blockList.forEachIndexed { index, block ->
            val parentBlock = parentList[index]

            // Dynamically select mining tool based on parent block's sound group
            val tagMethod =
                when (parentBlock.defaultState.soundGroup) {
                    // Stone-like groups (Pickaxe)
                    BlockSoundGroup.STONE -> ::mineableByPickaxe
                    BlockSoundGroup.BASALT -> ::mineableByPickaxe
                    BlockSoundGroup.DEEPSLATE -> ::mineableByPickaxe
                    BlockSoundGroup.DEEPSLATE_BRICKS -> ::mineableByPickaxe
                    BlockSoundGroup.DEEPSLATE_TILES -> ::mineableByPickaxe
                    BlockSoundGroup.POLISHED_DEEPSLATE -> ::mineableByPickaxe
                    BlockSoundGroup.TUFF -> ::mineableByPickaxe
                    BlockSoundGroup.POLISHED_TUFF -> ::mineableByPickaxe
                    BlockSoundGroup.TUFF_BRICKS -> ::mineableByPickaxe
                    BlockSoundGroup.NETHER_BRICKS -> ::mineableByPickaxe
                    BlockSoundGroup.AMETHYST_BLOCK -> ::mineableByPickaxe
                    BlockSoundGroup.DRIPSTONE_BLOCK -> ::mineableByPickaxe
                    BlockSoundGroup.COPPER -> ::mineableByPickaxe
                    BlockSoundGroup.METAL -> ::mineableByPickaxe
                    BlockSoundGroup.NETHERITE -> ::mineableByPickaxe
                    BlockSoundGroup.ANVIL -> ::mineableByPickaxe
                    BlockSoundGroup.LODESTONE -> ::mineableByPickaxe
                    BlockSoundGroup.CHAIN -> ::mineableByPickaxe
                    BlockSoundGroup.NETHER_ORE -> ::mineableByPickaxe
                    BlockSoundGroup.NETHERRACK -> ::mineableByPickaxe
                    BlockSoundGroup.SCULK -> ::mineableByPickaxe
                    BlockSoundGroup.IRON -> ::mineableByPickaxe

                    // Wood-like groups (Axe)
                    BlockSoundGroup.WOOD -> ::mineableByAxe
                    BlockSoundGroup.MANGROVE_ROOTS -> ::mineableByAxe
                    BlockSoundGroup.CHERRY_WOOD -> ::mineableByAxe
                    BlockSoundGroup.BAMBOO_WOOD -> ::mineableByAxe
                    BlockSoundGroup.NETHER_WOOD -> ::mineableByAxe
                    BlockSoundGroup.AZALEA -> ::mineableByAxe
                    BlockSoundGroup.AZALEA_LEAVES -> ::mineableByAxe
                    BlockSoundGroup.CHERRY_LEAVES -> ::mineableByAxe
                    BlockSoundGroup.STEM -> ::mineableByAxe
                    BlockSoundGroup.HANGING_SIGN -> ::mineableByAxe
                    BlockSoundGroup.NETHER_WOOD_HANGING_SIGN -> ::mineableByAxe
                    BlockSoundGroup.CHERRY_WOOD_HANGING_SIGN -> ::mineableByAxe

                    // Dirt/Sand-like groups (Shovel)
                    BlockSoundGroup.GRASS -> ::mineableByShovel
                    BlockSoundGroup.MOSS_BLOCK -> ::mineableByShovel
                    BlockSoundGroup.MOSS_CARPET -> ::mineableByShovel
                    BlockSoundGroup.ROOTED_DIRT -> ::mineableByShovel
                    BlockSoundGroup.GRAVEL -> ::mineableByShovel
                    BlockSoundGroup.SAND -> ::mineableByShovel
                    BlockSoundGroup.MUD -> ::mineableByShovel
                    BlockSoundGroup.MUD_BRICKS -> ::mineableByShovel
                    BlockSoundGroup.PACKED_MUD -> ::mineableByShovel
                    BlockSoundGroup.SNOW -> ::mineableByShovel
                    BlockSoundGroup.SOUL_SAND -> ::mineableByShovel
                    BlockSoundGroup.SOUL_SOIL -> ::mineableByShovel
                    BlockSoundGroup.WET_GRASS -> ::mineableByShovel
                    BlockSoundGroup.MUDDY_MANGROVE_ROOTS -> ::mineableByShovel

                    else -> {
                        logger.warn(
                            """
                    Unhandled Sound Group for Block Tagging
                    =====================================
                    Block: $block
                    Parent Block: $parentBlock
                   
                    Recommendation: 
                    - Update BlockTagGenerator to handle this specific sound group
                    - Add a new case in the when statement for this block type
                """,
                        )
                        defaultTag
                    }
                }

            // Apply the determined mining tag
            tagMethod(block)
        }
    }

    /** Marks a block as mineable by pickaxe */
    private fun mineableByPickaxe(block: Block) {
        addToTag(block, BlockTags.PICKAXE_MINEABLE)
    }

    /** Marks a block as mineable by axe */
    private fun mineableByAxe(block: Block) {
        addToTag(block, BlockTags.AXE_MINEABLE)
    }

    /** Marks a block as mineable by shovel */
    private fun mineableByShovel(block: Block) {
        addToTag(block, BlockTags.SHOVEL_MINEABLE)
    }

    private fun addToTag(
        block: Block,
        key: TagKey<Block>,
    ) {
        getOrCreateTagBuilder(key).add(block)
    }
}
