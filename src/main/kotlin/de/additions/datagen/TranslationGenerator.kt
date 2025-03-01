package de.additions.datagen

import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry
import de.additions.blocks.RedstoneLantern
import de.additions.items.ItemRegistry
import de.additions.items.RadiusMineItem
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.block.Block
import net.minecraft.block.LanternBlock
import net.minecraft.block.SlabBlock
import net.minecraft.block.StairsBlock
import net.minecraft.block.TrapdoorBlock
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture
import kotlin.text.trim

class TranslationGenerator(
    generator: FabricDataOutput,
    registryLookup: CompletableFuture<RegistryWrapper.WrapperLookup>,
) : FabricLanguageProvider(generator, registryLookup) {
    private val processedKey = mutableListOf<String>()
    private lateinit var builder: TranslationBuilder

    private fun add(
        key: String,
        value: String,
    ) {
        if (processedKey.contains(key)) {
            logger.warn("Duplicate key: $key")
        } else {
            processedKey.add(key)
            builder.add(key, value)
        }
    }

    private fun extractNameFromKey(translationKey: String): String =
        translationKey
            .split(".")
            .last()
            .split("_")
            .joinToString(" ") {
                it.replaceFirstChar { it.uppercase() }
            }.replace("Block", "")
            .replace("Item", "")
            .trim()

    override fun generateTranslations(
        registryLookup: RegistryWrapper.WrapperLookup,
        translationBuilder: TranslationBuilder,
    ) {
        builder = translationBuilder

        blockTranslationBuilder(BlockRegistry.registeredStairs, BlockRegistry.blockVariantsParents)
        blockTranslationBuilder(BlockRegistry.registeredSlabs, BlockRegistry.blockVariantsParents)
        blockTranslationBuilder(
            BlockRegistry.registeredTrapdoors,
            BlockRegistry.trapdoorVariantsParents,
        )
        blockTranslationBuilder(
            BlockRegistry.registeredLanterns.keys.toList(),
            BlockRegistry.registeredLanterns.values.toList(),
        )
        configTranslationbuilder()
        modMenuTranslationBuilder()

        itemsTranslationBuilder(ItemRegistry.registeredItems)

        toolTipTranslationBuilder(ItemRegistry.registeredItems)

        add("itemGroup.additions.blocks", "DayOfMind Blocks")
        add("itemGroup.additions.items", "DayOfMind Items")

        add("tag.item.additions.stones", "Stones")
        add("tag.item.additions.stones.tooltip", "All stone variants")
        add("item.minecraft.redstone_chain", "Redstone Chain")
    }

    private fun toolTipTranslationBuilder(items: MutableList<ItemStack>) {
        items.forEach { stack ->
            val item = stack.item

            if (item is RadiusMineItem) {
                val tooltip = item.getTooltip()
                tooltip.forEach { (key, desc) ->
                    add(key, desc)
                }
            }
        }
    }

    private fun itemsTranslationBuilder(stacks: MutableList<ItemStack>) {
        stacks.forEach { stack ->
            val itemName = extractNameFromKey(stack.item.translationKey)
            add(stack.item.translationKey, itemName)
        }
    }

    private fun modMenuTranslationBuilder() {
        val menuKey: String = "modmenu.additions"
        add("$menuKey.crowdin", "Help translate on Crowdin")
        add("$menuKey.discord", "Join the Discord")
        add("$menuKey.github", "View on GitHub")
        add("$menuKey.oxfatech", "OxFaTech Website")
        add("$menuKey.kofi", "Support us on Ko-fi")
    }

    private fun configTranslationbuilder() {
        val configKey: String = "additions.midnightconfig"
        add("$configKey.title", "DayOfMind Config")
        add("$configKey.category.about", "About DayOfMind")
        add("$configKey.category.features", "Features")
        add("$configKey.EnabledInstruments", "Enable Instruments")
        add("$configKey.DayOfMind", "DayOfMind")
        add(
            "$configKey.aboutDayOfMind",
            "DayOfMind is a mod that adds new blocks, recipes and features. With unique lanterns, expanded block variations and clever features like switching grass and dirt paths with a shovel, DayOfMind offers exciting possibilities for your adventures.",
        )
        add(
            "$configKey.features",
            "The settings listed here are fully developed and can be used safely.",
        )
        add("$configKey.EnabledInstruments.tooltip", "Enables the ability to craft instruments")
        add("$configKey.EnabledShovelMixin", "Enable Shovel Mixin")
        add(
            "$configKey.EnabledShovelMixin.tooltip",
            "Enables the ability to switch grass and dirt paths with a shovel",
        )
        add("$configKey.EnabledBlockVariants", "Enable Block Variants")
        add(
            "$configKey.EnabledBlockVariants.tooltip",
            "Enables the ability to craft more block variants (stairs, slabs)",
        )
        add("$configKey.EnabledLantern", "Enable Lantern")
        add("$configKey.EnabledLantern.tooltip", "Enables the ability to craft more lanterns")
        add("$configKey.EnabledRedstoneLantern", "Enable Redstone Lantern")
        add(
            "$configKey.EnabledRedstoneLantern.tooltip",
            "Enables the ability to craft redstone lanterns",
        )
        add("$configKey.EnabledTranslation", "Enable Translation")
        add(
            "$configKey.EnabledTranslation.tooltip",
            "Enables the automatic download of translations",
        )
        add("$configKey.TranslationUrl", "Translation Source")
        add("$configKey.TranslationUrl.tooltip", "Select the source of the translations")
        add("$configKey.TranslationUrlCustom", "Custom Translation Source")
        add("$configKey.TranslationUrlCustom.tooltip", "Enter the URL of the custom translation source")
        add("$configKey.enum.TranslationVersion.CROWDIN", "Crowdin")
        add("$configKey.enum.TranslationVersion.OXFATECH", "OxFaTech")
        add("$configKey.enum.TranslationVersion.CUSTOM", "Custom")
        add("$configKey.TranslationLogging", "Translation Logging")
        add(
            "$configKey.TranslationLogging.tooltip",
            "Enables detailed logging of the translation download. You probably don't want to enable this",
        )
        add("$configKey.EnabledTrapdoor", "Enable Trapdoor")
        add("$configKey.EnabledTrapdoor.tooltip", "Enables the ability to craft more trapdoors")
    }

    private fun blockTranslationBuilder(
        blockList: List<Block>,
        parentBlockList: List<Block>,
    ) {
        blockList.forEachIndexed { index, block ->
            val parentName = extractNameFromKey(parentBlockList[index].translationKey)
            val blockType =
                when (block) {
                    is StairsBlock -> "Stairs"
                    is SlabBlock -> "Slab"
                    is TrapdoorBlock -> "Trapdoor"
                    is RedstoneLantern -> "Redstone Lantern"
                    is LanternBlock -> "Lantern"
                    else -> "Block"
                }
            add(block.asItem().translationKey, "$parentName $blockType")
        }
    }
}
