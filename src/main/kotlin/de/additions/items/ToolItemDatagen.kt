package de.additions.items


import de.additions.Additions.logger
import de.additions.datagen.ItemTagGenerator
import net.minecraft.core.HolderGetter
import net.minecraft.tags.ItemTags
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient

/**
 * Retrieves the crafting ingredient associated with the tool's material, returning either a
 * block tag or a specific item. For materials like wood and stone, it returns a [TagKey]
 * representing a group of blocks (e.g., planks or stones). For metal-based materials such
 * as iron, diamond, gold, or netherite, it returns the corresponding ingot item.
 *
 * @return A [Pair] containing an optional [TagKey] for block tags and an optional [Item]
 * for specific items.
 */
fun ToolItem.getCraftingTagOrItem(): Pair<TagKey<Item>?, Item?> =
    when (material) {
        ToolItem.C_WOOD -> Pair(ItemTags.PLANKS, null)
        ToolItem.C_STONE -> Pair(ItemTagGenerator.stonesTag, null)
        ToolItem.C_IRON -> Pair(null, Items.IRON_INGOT)
        ToolItem.C_DIAMOND -> Pair(null, Items.DIAMOND)
        ToolItem.C_GOLD -> Pair(null, Items.GOLD_INGOT)
        ToolItem.C_NETHERITE -> Pair(null, Items.NETHERITE_INGOT)
        else -> {
            logger.warn("${ToolItem.UNKNOWN_MATERIAL_MSG} (from getCraftingTagOrItem: $material)")
            Pair(ItemTags.PLANKS, null)
        }
    }

/**
 * Retrieves the ingredient required for crafting or representing the tool's material.
 * This method checks for a specific crafting tag, then an individual item, and falls back to
 * a default ingredient (planks) if no valid material component is identified.
 *
 * @param registryLookup The holder getter used to resolve item tags or items within the registry.
 * @return An [Ingredient] representing the tool's material component.
 */
fun ToolItem.getMaterialIngredient(registryLookup: HolderGetter<Item>): Ingredient {
    val (tag, item) = getCraftingTagOrItem()
    return when {
        tag != null -> Ingredient.of(registryLookup.getOrThrow(tag))
        item != null -> Ingredient.of(item)
        else -> {
            logger.warn("${ToolItem.UNKNOWN_MATERIAL_MSG} (from getMaterialIngredient: $material)")
            Ingredient.of(registryLookup.getOrThrow(ItemTags.PLANKS))
        }
    }
}

/**
 * Retrieves the string identifier for the current tool's material by searching through the registered materials.
 *
 * @return The name of the material, or "unknown" if no matching material is found in the registry.
 */
fun ToolItem.getMaterialName(): String =
    ToolItem.materials.entries.firstOrNull { it.value == material }?.key
        ?: run {
            logger.warn("${ToolItem.UNKNOWN_MATERIAL_MSG} (from getMaterialName: $material)")
            "unknown"
        }