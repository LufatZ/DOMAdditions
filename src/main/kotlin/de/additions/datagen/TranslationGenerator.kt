@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen

import de.additions.Additions.logger
import de.additions.blocks.BlockRegistry
import de.additions.blocks.lanterns.*
import de.additions.items.ItemRegistry
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider
import net.minecraft.block.*
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryWrapper
import java.util.concurrent.CompletableFuture

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

    /**
     * Extracts a human-readable name from a translation key by processing its components.
     *
     * This method handles various transformations based on the input key:
     * - Splits the key after the last dot and processes each segment
     * - Converts underscores to spaces and capitalizes words
     * - Removes certain suffixes like "Block" or "Item"
     * - Handles special cases for tool names (shovel, hammer, etc.)
     * - Applies specific transformations for material types (wood → wooden, gold → golden)
     * - Processes the "big axe" case to produce "Lumberjack Axe"
     *
     * @param translationKey The input key string containing the name components separated by dots and underscores
     * @return The processed human-readable name with proper capitalization and transformations applied
     */
    private fun extractNameFromKey(translationKey: String): String {
        val base = translationKey
            .substringAfterLast(".")
            .split("_")
            .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
            .replace(Regex("\\b(Block|Item)\\b"), "")
            .trim()

        if (!translationKey.startsWith("item.")) return base

        val toolSuffixes = setOf("Shovel", "Hammer", "Pickaxe", "Axe", "Sword", "Hoe", "Spade", "Pick")
        val words = base.split(" ")
        if (words.lastOrNull() !in toolSuffixes) return base

        return base
            .replace(Regex("\\bWood\\b"), "Wooden")
            .replace(Regex("\\bGold\\b"), "Golden")
            .run {
                if ("big_" in translationKey && words.last() == "Axe") {
                    replace(Regex("\\bBig\\s+"), "")
                        .replace(Regex("\\bAxe\\b"), "Lumberjack Axe")
                } else this
            }
    }

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
        chainTranslationBuilder()


        itemsTranslationBuilder(ItemRegistry.registeredItems)

        add("tooltip.additions.radius_mine.shovel_description", "Mines a %s area of earth-like blocks.")
        add("tooltip.additions.radius_mine.hammer_description", "Mines a %s area of stone-like blocks.")
        add("tooltip.additions.radius_mine.axe_description", "Strips logs in a %s area.")
        add("tooltip.additions.radius_mine.stripped_wood_creation_description", "Fells an entire tree at once.")
        add("tooltip.additions.radius_mine.pickaxe_description", "Mines all ore blocks at once.")
        add("tooltip.additions.radius_mine.path_creation_description", "Use to create a path.")
        add("tooltip.additions.radius_mine.sneak_description", "Sneak-use to change path to dirt.")

        add("itemGroup.additions.blocks", "DayOfMind Blocks")
        add("itemGroup.additions.items", "DayOfMind Items")

        add("tag.item.additions.stones", "Stones")
        add("tag.item.additions.stones.tooltip", "All stone variants")
    }

    private fun itemsTranslationBuilder(stacks: MutableList<ItemStack>) {
        stacks.forEach { stack ->
            val itemName = extractNameFromKey(stack.item.translationKey)
            add(stack.item.translationKey, itemName)
        }
    }

    private fun modMenuTranslationBuilder() {
        val menuKey = "modmenu.additions"
        add("$menuKey.crowdin", "Help translate on Crowdin")
        add("$menuKey.discord", "Join the Discord")
        add("$menuKey.github", "View on GitHub")
        add("$menuKey.oxfatech", "OxFaTech Website")
        add("$menuKey.kofi", "Support us on Ko-fi")
    }

    private fun configTranslationbuilder() {
        val configKey = "additions.midnightconfig"
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
        add("$configKey.EnabledShovelMixin", "Enable Shovel Path Toggle")
        add(
            "$configKey.EnabledShovelMixin.tooltip",
            "Allows toggling between Grass Block and Dirt Path with a shovel",
        )
        add("$configKey.EnabledBlockVariants", "Enable Block Variants")
        add(
            "$configKey.EnabledBlockVariants.tooltip",
            "Enables the ability to craft more block variants (stairs, slabs)",
        )
        add("$configKey.EnabledLantern", "Enable Lanterns")
        add("$configKey.EnabledLantern.tooltip", "Enables the ability to craft more lanterns")
        add("$configKey.EnabledRedstoneLantern", "Enable Redstone Lanterns")
        add(
            "$configKey.EnabledRedstoneLantern.tooltip",
            "Enables the ability to craft redstone lanterns",
        )
        add("$configKey.EnabledTranslation", "Enable Translations")
        add(
            "$configKey.EnabledTranslation.tooltip",
            "Enables the automatic download of translations",
        )
        add("$configKey.TranslationCounter", "Starts for translation download")
        add(
            "$configKey.TranslationCounter.tooltip",
            "enter the number of game starts at which you'd like to pause translation updates.",
        )
        add("$configKey.TranslationUrl", "Translation Source")
        add("$configKey.TranslationUrl.tooltip", "Select the source of the translations")
        add("$configKey.TranslationUrlCustom", "Custom Translation Source")
        add("$configKey.TranslationUrlCustom.tooltip", "Enter the URL of the custom translation source")
        add("$configKey.enum.TranslationVersion.CROWDIN", "Crowdin")
        add("$configKey.enum.TranslationVersion.OXFATECH", "OxFaTech")
        add("$configKey.enum.TranslationVersion.CUSTOM", "Custom")
        add("$configKey.DetailedLogging", "Detailed Logging")
        add(
            "$configKey.DetailedLogging.tooltip",
            "Enables detailed logging of this mod, such as translation downloads. You probably don't want to enable this because of log spamming.",
        )
        add("$configKey.EnabledTrapdoor", "Enable Trapdoors")
        add("$configKey.EnabledTrapdoor.tooltip", "Enables the ability to craft more trapdoors")

        add("$configKey.EnableFastLeafDecay", "Enable Fast Leaf Decay")
        add(
            "$configKey.EnableFastLeafDecay.tooltip",
            "Enables faster decay of leaves.\nWhen disabled, vanilla behavior is used.",
        )
        add("$configKey.LeafDecayDelay", "Leaf Decay Delay")
        add(
            "$configKey.LeafDecayDelay.tooltip",
            "Delay (in ticks) before leaves update their distance from logs.\n§aLower values§r = faster decay, but §cmay cause lag§r in large forests.\n§eRecommended§r: 1–3",
        )
    }

    private fun extractOxidationPrefix(translationKey: String): String {
        val keyParts = translationKey.split(".").last()

        return when {
            keyParts.startsWith("waxed_oxidized_") -> "Waxed Oxidized "
            keyParts.startsWith("waxed_weathered_") -> "Waxed Weathered "
            keyParts.startsWith("waxed_exposed_") -> "Waxed Exposed "
            keyParts.startsWith("waxed_") -> "Waxed "
            keyParts.startsWith("oxidized_") -> "Oxidized "
            keyParts.startsWith("weathered_") -> "Weathered "
            keyParts.startsWith("exposed_") -> "Exposed "
            else -> ""
        }
    }
    private fun chainTranslationBuilder() {
        BlockRegistry.registeredChains.forEach { chain ->
            add(chain.asItem().translationKey, extractNameFromKey(chain.translationKey))
        }
    }

    private fun blockTranslationBuilder(
        blockList: List<Block>,
        parentBlockList: List<Block>,
    ) {
        val nounMaterials = setOf("Gold", "Iron", "Copper", "Diamond", "Emerald", "Netherite")

        blockList.forEachIndexed { index, block ->
            val parentName = extractNameFromKey(parentBlockList[index].translationKey)
            val oxidationPrefix = extractOxidationPrefix(block.asItem().translationKey)

            val blockType =
                when (block) {
                    is StairsBlock -> "Stairs"
                    is SlabBlock -> "Slab"
                    is TrapdoorBlock -> "Trapdoor"
                    is LanternBlock -> "Lantern"
                    else -> "Block"
                }

            val prefix =
                when (block) {
                    is SmallLantern, is SmallRedstoneLantern -> "Small "
                    is BigLantern, is BigRedstoneLantern -> "Big "
                    else -> ""
                }

            val type =
                when (block) {
                    is RedstoneLantern -> "Redstone "
                    else -> ""
                }

            val name = if (blockType in listOf("Stairs", "Slab", "Trapdoor") && parentName in nounMaterials) {
                oxidationPrefix + prefix + type + blockType.dropLast(if (blockType.endsWith("s")) 1 else 0) + " of " + parentName
            } else {
                "$oxidationPrefix$prefix$parentName $type$blockType"
            }

            add(block.asItem().translationKey, name)
        }
    }
}
