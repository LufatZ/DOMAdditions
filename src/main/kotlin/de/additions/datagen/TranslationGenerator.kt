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
        blockTranslationBuilder(
            BlockRegistry.registeredTrapdoors,
            BlockRegistry.trapdoorVariantsParents,
            translationBuilder
        )
        blockTranslationBuilder(
            BlockRegistry.registeredLanterns,
            BlockRegistry.lanternVariantsParents,
            translationBuilder
        )

        translationBuilder.add("itemGroup.additions.blocks", "DayOfMind Blocks")
        translationBuilder.add("itemGroup.additions.items", "DayOfMind Items")

        val menuKey: String = "modmenu.additions"
        translationBuilder.add("$menuKey.crowdin", "Help translate on Crowdin")
        translationBuilder.add("$menuKey.discord", "Join the Discord")
        translationBuilder.add("$menuKey.github", "View on GitHub")
        translationBuilder.add("$menuKey.oxfatech", "OxFaTech Website")
        translationBuilder.add("$menuKey.kofi", "Support us on Ko-fi")

        val configKey: String = "additions.midnightconfig"
        translationBuilder.add("$configKey.title", "DayOfMind Config")
        translationBuilder.add("$configKey.category.about", "About DayOfMind")
        translationBuilder.add("$configKey.category.features", "Features")
        translationBuilder.add("$configKey.EnabledInstruments", "Enable Instruments")
        translationBuilder.add("$configKey.DayOfMind", "DayOfMind")
        translationBuilder.add(
            "$configKey.aboutDayOfMind",
            "DayOfMind is a mod that adds new blocks, recipes and features. With unique lanterns, expanded block variations and clever features like switching grass and dirt paths with a shovel, DayOfMind offers exciting possibilities for your adventures."
        )
        translationBuilder.add(
            "$configKey.features",
            "The settings listed here are fully developed and can be used safely."
        )
        translationBuilder.add("$configKey.EnabledInstruments.tooltip", "Enables the ability to craft instruments")
        translationBuilder.add("$configKey.EnabledShovelMixin", "Enable Shovel Mixin")
        translationBuilder.add(
            "$configKey.EnabledShovelMixin.tooltip",
            "Enables the ability to switch grass and dirt paths with a shovel"
        )
        translationBuilder.add("$configKey.EnabledBlockVariants", "Enable Block Variants")
        translationBuilder.add(
            "$configKey.EnabledBlockVariants.tooltip",
            "Enables the ability to craft more block variants (stairs, slabs)"
        )
        translationBuilder.add("$configKey.EnabledLantern", "Enable Lantern")
        translationBuilder.add("$configKey.EnabledLantern.tooltip", "Enables the ability to craft more lanterns")
        translationBuilder.add("$configKey.EnabledRedstoneLantern", "Enable Redstone Lantern")
        translationBuilder.add(
            "$configKey.EnabledRedstoneLantern.tooltip",
            "Enables the ability to craft redstone lanterns"
        )
        translationBuilder.add("$configKey.EnabledTranslation", "Enable Translation")
        translationBuilder.add(
            "$configKey.EnabledTranslation.tooltip",
            "Enables the automatic download of translations"
        )
        translationBuilder.add("$configKey.TranslationUrl", "Translation Source")
        translationBuilder.add("$configKey.TranslationUrl.tooltip", "Select the source of the translations")
        translationBuilder.add("$configKey.TranslationUrlCustom", "Custom Translation Source")
        translationBuilder.add("$configKey.TranslationUrlCustom.tooltip", "Enter the URL of the custom translation source")
        translationBuilder.add("$configKey.enum.TranslationVersion.CROWDIN", "Crowdin")
        translationBuilder.add("$configKey.enum.TranslationVersion.OXFATECH", "OxFaTech")
        translationBuilder.add("$configKey.enum.TranslationVersion.CUSTOM", "Custom")
        translationBuilder.add("$configKey.TranslationLogging", "Translation Logging")
        translationBuilder.add("$configKey.TranslationLogging.tooltip", "Enables detailed logging of the translation download. You probably don't want to enable this")
        translationBuilder.add("$configKey.EnabledTrapdoor", "Enable Trapdoor")
        translationBuilder.add("$configKey.EnabledTrapdoor.tooltip", "Enables the ability to craft more trapdoors")
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