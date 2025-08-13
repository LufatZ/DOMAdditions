@file:Suppress("ktlint:standard:no-wildcard-imports")

package de.additions.datagen

import de.additions.Additions.logger
import de.additions.blocks.*
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
     * Erzeugt einen lesbaren Namen aus dem letzten Segment des translationKey.
     * - Wandelt snake_case -> Title Case
     * - Entfernt ggf. "Block"/"Item" Reste
     * - Für ITEM-Keys: Normalisiert Materialbezeichnungen **nur für Werkzeuge**:
     *   "Wood" -> "Wooden", "Gold" -> "Golden" (nur wenn das Item ein Werkzeug/Typ ist)
     *
     * Rationale: Vanilla verwendet unlogische, aber etablierte Patterns (z.B. "Wooden Shovel",
     * "Golden Shovel" bei Tools; bei Blöcken bleibt "Gold" z.B. "Block of Gold").
     */
    private fun extractNameFromKey(translationKey: String): String {
        val base = translationKey
            .split(".")
            .last()
            .split("_")
            .joinToString(" ") {
                it.replaceFirstChar { it.uppercase() }
            }.replace(Regex("\\bBlock\\b"), "")
            .replace(Regex("\\bItem\\b"), "")
            .trim()

        if (translationKey.startsWith("item.")) {
            val toolSuffixes = setOf("Shovel", "Hammer", "Pickaxe", "Axe", "Sword", "Hoe", "Spade", "Pick")
            val lastWord = base.split(" ").lastOrNull() ?: ""

            if (toolSuffixes.contains(lastWord)) {
                return base
                    .replace(Regex("\\bWood\\b"), "Wooden")
                    .replace(Regex("\\bGold\\b"), "Golden")
            }
        }

        return base
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

        itemsTranslationBuilder(ItemRegistry.registeredItems)

        add("tooltip.additions.radius_mine.shovel_description", "Mines a %s area of earth-like blocks.")
        add("tooltip.additions.radius_mine.hammer_description", "Mines a %s area of stone-like blocks.")
        add("tooltip.additions.radius_mine.path_creation_description", "Use to create a path.")
        add("tooltip.additions.radius_mine.sneak_description", "Sneak-use to change path to dirt.")

        add("itemGroup.additions.blocks", "DayOfMind Blocks")
        add("itemGroup.additions.items", "DayOfMind Items")

        add("tag.item.additions.stones", "Stones")
        add("tag.item.additions.stones.tooltip", "All stone variants")
        add("item.minecraft.redstone_chain", "Redstone Chain")
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
    }

    private fun blockTranslationBuilder(
        blockList: List<Block>,
        parentBlockList: List<Block>,
    ) {
        val nounMaterials = setOf("Gold", "Iron", "Copper", "Diamond", "Emerald", "Netherite") // ggf. erweitern

        blockList.forEachIndexed { index, block ->
            val parentName = extractNameFromKey(parentBlockList[index].translationKey)
            val blockType =
                when (block) {
                    is StairsBlock -> "Stairs" //Strictly speaking, this is not logical. The reason is linguistic habit: ‘Stairs’ is often a singular word in English, similar to ‘scissors’.
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
                prefix + type + blockType.dropLast(if (blockType.endsWith("s")) 1 else 0) + " of " + parentName
            } else {
                prefix + parentName + " " + type + blockType
            }
            add(block.asItem().translationKey, name)
        }
    }
}
