package de.additions.datagen

import de.additions.blocks.BlockRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.block.Block
import net.minecraft.block.LanternBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.block.TrapdoorBlock
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture
import kotlin.text.trim

class TranslationGenerator(generator: FabricDataOutput, registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>) : FabricLanguageProvider(generator,registryLookup) {
    override fun generateTranslations(
        registryLookup: RegistryWrapper.WrapperLookup,
        translationBuilder: TranslationBuilder
    ) {
        blockTranslationBuilder(BlockRegistry.registeredStairs, BlockRegistry.blockVariantsParents, translationBuilder)
        blockTranslationBuilder(BlockRegistry.registeredSlabs, BlockRegistry.blockVariantsParents, translationBuilder)
        blockTranslationBuilder(BlockRegistry.registeredTrapdoors, BlockRegistry.trapdoorVariantsParents, translationBuilder)
        blockTranslationBuilder(BlockRegistry.registeredLanterns, BlockRegistry.lanternVariantsParents, translationBuilder)

        translationBuilder.add("itemGroup.additions.blocks", "DayOfMind Blocks")
        translationBuilder.add("itemGroup.additions.items", "DayOfMind Items")
    }

    fun extractBlockName(translationKey: String): String {
        return translationKey.split(".").last().split("_").joinToString(" ") { it.replaceFirstChar { it.uppercase() } }.replace("Block", "").trim()
    }
    fun blockTranslationBuilder(blockList: List<Block>, parentBlockList: List<Block>, translationBuilder: TranslationBuilder ) {
        blockList.forEachIndexed { index, block ->
            val parentName = extractBlockName(parentBlockList[index].translationKey)
            val blockType = when (block) {
                is StairsBlock -> "Stairs"
                is SlabBlock -> "Slab"
                is TrapdoorBlock -> "Trapdoor"
                is LanternBlock -> "Lantern"
                else -> "Block"
            }
            translationBuilder.add(block.asItem(), "$parentName $blockType")
        }
    }

}