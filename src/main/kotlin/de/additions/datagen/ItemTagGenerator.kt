package de.additions.datagen

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry
import de.additions.helper.IdentifierHelper
import de.additions.items.ItemRegistry
import de.additions.items.RadiusMineItem
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.BlockTags
import net.minecraft.registry.tag.ItemTags
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

/**
 * Generates item tags
 *
 * @property generator The Fabric data output generator
 * @property registryLookup Asynchronous registry wrapper for block lookups
 */
class ItemTagGenerator(
    generator: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricTagProvider<Item>(generator, RegistryKeys.ITEM, registryLookup) {

    companion object {
        val stonesTag = TagKey.of(RegistryKeys.ITEM, Identifier.of(MODID, "stones"))
    }

    /**
     * Configures item tags for different items (also blockItems).
     *
     * @param wrapper Registry wrapper providing block lookup capabilities
     */
    override fun configure(wrapper: RegistryWrapper.WrapperLookup) {
        stonesTag()
        BlockRegistry.registeredSlabs.forEach{ slab -> addToTag(slab, ItemTags.SLABS) }
        BlockRegistry.registeredStairs.forEach{ stair -> addToTag(stair, ItemTags.STAIRS) }
        BlockRegistry.registeredTrapdoors.forEach{ trapdoor -> addToTag( trapdoor, ItemTags.TRAPDOORS) }

        ItemRegistry.registeredItems.forEach{ stack ->
            val item = stack.item
            if (item is RadiusMineItem) {
                // Check mineable type
                when (item.effectiveBlocks) {
                    BlockTags.SHOVEL_MINEABLE -> addToTag(item, ItemTags.SHOVELS)
                    BlockTags.PICKAXE_MINEABLE -> addToTag(item, ItemTags.PICKAXES)
                }

                // Check material type
                when (item.material) {
                    RadiusMineItem.C_WOOD -> addToTag(item, ItemTags.WOODEN_TOOL_MATERIALS)
                    RadiusMineItem.C_STONE -> addToTag(item, ItemTags.STONE_TOOL_MATERIALS)
                    RadiusMineItem.C_IRON -> addToTag(item, ItemTags.IRON_TOOL_MATERIALS)
                    RadiusMineItem.C_DIAMOND -> addToTag(item, ItemTags.DIAMOND_TOOL_MATERIALS)
                    RadiusMineItem.C_GOLD -> addToTag(item, ItemTags.GOLD_TOOL_MATERIALS)
                    RadiusMineItem.C_NETHERITE -> addToTag(item, ItemTags.NETHERITE_TOOL_MATERIALS)
                }
            } else {
                logger.warn("no item tag preset for $item")
            }
        }
    }

    private fun stonesTag() {
        val stones = listOf<Block>(
            Blocks.STONE, Blocks.GRANITE, Blocks.POLISHED_GRANITE, Blocks.DIORITE,
            Blocks.POLISHED_DIORITE, Blocks.ANDESITE, Blocks.POLISHED_ANDESITE,
            Blocks.TUFF, Blocks.POLISHED_TUFF, Blocks.TUFF_BRICKS,
            Blocks.BASALT, Blocks.POLISHED_BASALT, Blocks.BLACKSTONE,
            Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.GILDED_BLACKSTONE,
            Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE, Blocks.POLISHED_DEEPSLATE,
            Blocks.DEEPSLATE_BRICKS, Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_BRICKS,
            Blocks.CRACKED_DEEPSLATE_TILES, Blocks.CHISELED_DEEPSLATE,
            Blocks.CALCITE, Blocks.STONE_BRICKS, Blocks.MOSSY_STONE_BRICKS,
            Blocks.CRACKED_STONE_BRICKS, Blocks.CHISELED_STONE_BRICKS, Blocks.INFESTED_STONE,
            Blocks.INFESTED_COBBLESTONE, Blocks.INFESTED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS,
            Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.SMOOTH_BASALT,
            Blocks.COBBLESTONE,Blocks.MOSSY_COBBLESTONE
        )
        stones.forEach { stone -> addToTag(stone, stonesTag) }
    }
    private fun addToTag(
        item: Item,
        key: TagKey<Item>
    ) {
        getTagBuilder(key).add(IdentifierHelper.getId(item))
    }
    private fun addToTag(
        block: Block,
        key: TagKey<Item>
    ) {
        getTagBuilder(key).add(IdentifierHelper.getId(block))
    }
}