package de.additions.datagen

import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
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
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricTagProvider<Block>(generator, RegistryKeys.BLOCK, registryLookup) {

    /**
     * Configures block tags for different block variants.
     *
     * This method handles tag generation for:
     * - Lanterns (always pickaxe-mineable)
     * - Trapdoors (based on parent block type)
     * - Stairs and slabs (based on parent block type)
     *
     * @param wrapper Registry wrapper providing block lookup capabilities
     */
    override fun configure(wrapper: RegistryWrapper.WrapperLookup) {
        // Lanterns are always mineable by pickaxe
        BlockRegistry.registeredLanterns.forEach { lantern -> mineableByPickaxe(lantern) }

        // inherit mining tags from their parent blocks
        useParentMineable(BlockRegistry.registeredTrapdoors, BlockRegistry.trapdoorVariantsParents)
        useParentMineable(BlockRegistry.registeredStairs, BlockRegistry.blockVariantsParents)
        useParentMineable(BlockRegistry.registeredSlabs, BlockRegistry.blockVariantsParents)
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
        defaultTag: (Block) -> Unit = ::mineableByPickaxe
    ) {
        blockList.forEachIndexed { index, block ->
            val parentBlock = parentList[index]

            // Dynamically select mining tool based on parent block's sound group
            val tagMethod = when (parentBlock.defaultState.soundGroup) {
                BlockSoundGroup.WOOD -> ::mineableByAxe
                BlockSoundGroup.GRASS -> ::mineableByShovel
                BlockSoundGroup.ROOTED_DIRT -> ::mineableByShovel
                BlockSoundGroup.AMETHYST_BLOCK -> ::mineableByPickaxe
                else -> {
                    defaultTag
                }
            }

            // Apply the determined mining tag
            tagMethod(block)
        }
    }

    /** Marks a block as mineable by pickaxe */
    private fun mineableByPickaxe(block: Block) {
        getOrCreateTagBuilder(BlockTags.PICKAXE_MINEABLE).add(block)
    }

    /** Marks a block as mineable by axe */
    private fun mineableByAxe(block: Block) {
        getOrCreateTagBuilder(BlockTags.AXE_MINEABLE).add(block)
    }

    /** Marks a block as mineable by shovel */
    private fun mineableByShovel(block: Block) {
        getOrCreateTagBuilder(BlockTags.SHOVEL_MINEABLE).add(block)
    }
}