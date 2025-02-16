package de.additions.datagen.models

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.items.ItemRegistry.registeredItems
import de.additions.items.RadiusMineItem
import net.minecraft.client.data.ItemModelGenerator
import net.minecraft.client.data.ItemModels
import net.minecraft.client.data.Model
import net.minecraft.client.data.TextureKey
import net.minecraft.client.data.TextureMap
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier
import java.util.Optional

object ItemModelsGenerator {

    /**
     * Generates all item models from registered items.
     */
    fun ItemModelGenerator?.generateItemsModels() {
        registeredItems.forEach { stack ->
            when (val item = stack.item) {
                is RadiusMineItem -> generateRadiusMineToolModel(this, item)
                else -> logger.warn("Unknown item type: $item")
            }
        }
    }

    /**
     * Generates the item model for a RadiusMineItem.
     *
     * The model is based on a template (template_hammer/template_shovel) and replaces texture placeholders
     * "1" (material) and "particle" with dynamic textures derived from the item's material.
     *
     * @param generator The ItemModelGenerator instance (can be null)
     * @param item The RadiusMineItem to generate the model for
     */
    private fun generateRadiusMineToolModel(generator: ItemModelGenerator?, item: RadiusMineItem) {
        // Get the name of the material block (e.g., "gold_block")
        val materialBlock = item.getMaterialBlock()
            .defaultState
            .registryEntry
            .idAsString
            .replace("minecraft:", "")
        // Create identifier for dynamic material texture (e.g., "block/gold_block")
        val dynamicTexture = Identifier.ofVanilla("block/$materialBlock")
        logger.info("Using material texture: $dynamicTexture for item: ${item.name}")

        // Determine template model based on effective blocks
        val templateModelId = when(item.effectiveBlocks) {
            BlockTags.PICKAXE_MINEABLE -> Identifier.of(MODID, "item/template_hammer")
            BlockTags.SHOVEL_MINEABLE -> Identifier.of(MODID, "item/template_shovel")
            else -> Identifier.of(MODID, "item/template_hammer").also {
                logger.warn("Unknown effective block tag: ${item.effectiveBlocks}")
            }
        }

        // Define required texture keys (must use same instances)
        val materialKey = TextureKey.of("1")
        val particleKey = TextureKey.of("particle")

        // Create texture map (template already defines the "stick" part via key "0")
        val textureMap = TextureMap()
            .put(materialKey, dynamicTexture)
            .put(particleKey, dynamicTexture)

        // Create model with template parent and required texture keys
        val model = Model(
            Optional.of(templateModelId),
            Optional.empty(),
            materialKey,
            particleKey
        )

        // "Bake" (upload) the model with texture map - generates final JSON
        val uploadedModelId = model.upload(item, textureMap, generator?.modelCollector)

        // Register finished model for the item
        generator?.output?.accept(item, ItemModels.basic(uploadedModelId))
    }
}