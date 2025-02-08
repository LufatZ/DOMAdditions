package de.additions.datagen

import de.additions.Additions.MODID
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.RegistryWrapper
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
        stones.forEach { stone -> getOrCreateTagBuilder(stonesTag).add(stone.asItem()) }
    }


}