package de.additions.datagen.models

import de.additions.Additions.MODID
import de.additions.Additions.logger
import de.additions.helper.ModelHelper
import de.additions.items.ItemRegistry.registeredItems
import de.additions.items.RadiusMineItem
import de.additions.items.ToolItem
import de.additions.items.ToolItem.Companion.C_DIAMOND
import de.additions.items.ToolItem.Companion.C_GOLD
import de.additions.items.ToolItem.Companion.C_IRON
import de.additions.items.ToolItem.Companion.C_NETHERITE
import de.additions.items.ToolItem.Companion.C_STONE
import de.additions.items.ToolItem.Companion.C_WOOD
import de.additions.items.ToolItem.Companion.UNKNOWN_MATERIAL_MSG
import net.minecraft.world.level.block.Blocks
import net.minecraft.client.data.*
import net.minecraft.client.data.models.ItemModelGenerators
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.ModelTemplate
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.tags.BlockTags
import net.minecraft.resources.Identifier
import java.util.*

object ItemModelGenerator {
    enum class TextureType {
        HANDLE,
        TOOL,
        FINISHING,
        HANDLE_WRAPPING
    }
    /**
     * Generates all item models from registered items.
     */
    fun ItemModelGenerators.generateItemsModels() {
        registeredItems.forEach { stack ->
            when (val item = stack.item) {
                is ToolItem -> generateMineToolModel(this, item)
                else -> logger.warn("Unknown item type: $item")
            }
        }
    }

    /**
     * Generates a block identifier for the given tool's material and texture type.
     *
     * @param item The [ToolItem] whose material determines the base texture.
     * @param type The [TextureType] specifying which part of the tool the texture is for (e.g., handle, blade).
     * @return An [Identifier] pointing to the resolved block texture resource path.
     */
    fun getMaterialTextureIdentifier(item: ToolItem, type: TextureType): Identifier {
        var replace = listOf("", "")
        val blockForTexture = when (type) {
            TextureType.HANDLE -> when (item.material) {
                C_WOOD -> Blocks.STRIPPED_SPRUCE_WOOD.also { replace = listOf("wood", "log") }
                C_STONE -> Blocks.STRIPPED_SPRUCE_WOOD.also { replace = listOf("wood", "log") }
                C_IRON -> Blocks.IRON_BLOCK
                C_DIAMOND, C_GOLD -> Blocks.ANVIL
                C_NETHERITE -> Blocks.NETHERITE_BLOCK
                else -> null
            }
            TextureType.TOOL -> when (item.material) {
                C_WOOD -> Blocks.SPRUCE_LOG
                C_STONE -> Blocks.STONE
                C_IRON -> Blocks.ANVIL
                C_DIAMOND -> Blocks.DIAMOND_BLOCK
                C_GOLD -> Blocks.GOLD_BLOCK
                C_NETHERITE -> Blocks.NETHERITE_BLOCK
                else -> null
            }
            TextureType.FINISHING -> when (item.material) {
                C_WOOD -> Blocks.STONE
                C_STONE -> Blocks.IRON_BLOCK
                C_IRON -> Blocks.OBSIDIAN
                C_DIAMOND -> Blocks.CRYING_OBSIDIAN
                C_GOLD -> Blocks.IRON_BLOCK
                C_NETHERITE -> Blocks.AMETHYST_BLOCK
                else -> null
            }
            TextureType.HANDLE_WRAPPING -> when (item.material) {
                C_WOOD, C_STONE, C_IRON, C_DIAMOND, C_GOLD, C_NETHERITE -> Blocks.LIGHT_GRAY_WOOL
                else -> null
            }
        } ?: run {
            logger.warn("$UNKNOWN_MATERIAL_MSG (from getMaterialTextureIdentifier: ${item.material}, type: $type)")
            Blocks.OAK_PLANKS
        }

        val id = Identifier.withDefaultNamespace(
            "block/${ModelHelper.extractCleanBlockIdentifier(blockForTexture)
                .replace(replace[0], replace[1])}"
        )
        logger.info("Using material texture: $id for item: ${item.name}")
        return id
    }

    /**
     * Generates the item model for a ToolItem.
     *
     * The model is based on a template (template_hammer/template_shovel) and replaces texture placeholders
     * "1" (material) and "particle" with dynamic textures derived from the item's material.
     *
     * @param generator The ItemModelGenerator instance (can be null)
     * @param item The ToolItem to generate the model for
     */
    private fun generateMineToolModel(generator: ItemModelGenerators, item: ToolItem) {
        // Determine template model based on effective blocks
        val templateModelId = when(item.effectiveBlocks) {
            BlockTags.MINEABLE_WITH_PICKAXE ->
                if (item is RadiusMineItem)
                    Identifier.fromNamespaceAndPath(MODID, "item/template_hammer")
                else Identifier.fromNamespaceAndPath(MODID, "item/template_pickaxe")
            BlockTags.MINEABLE_WITH_SHOVEL -> Identifier.fromNamespaceAndPath(MODID, "item/template_shovel")
            BlockTags.MINEABLE_WITH_AXE -> Identifier.fromNamespaceAndPath(MODID, "item/template_lumberjack_axe")
            else -> Identifier.fromNamespaceAndPath(MODID, "item/template_hammer").also {
                logger.warn("Unknown effective block tag: ${item.effectiveBlocks}")
            }
        }

        // Define required texture keys (must use same instances)
        val handleMaterialKey = TextureSlot.create("0")
        val toolMaterialKey = TextureSlot.create("1")
        val finishingMaterialKey = TextureSlot.create("2")
        val handleWrappingMaterialKey = TextureSlot.create("3")
        val particleKey = TextureSlot.create("particle")

        // Create texture map (template already defines the "stick" part via key "0")
        val textureMap = TextureMapping()
            .put(handleMaterialKey, getMaterialTextureIdentifier(item, TextureType.HANDLE))
            .put(toolMaterialKey, getMaterialTextureIdentifier(item, TextureType.TOOL))
            .put(finishingMaterialKey, getMaterialTextureIdentifier(item, TextureType.FINISHING))
            .put(handleWrappingMaterialKey, getMaterialTextureIdentifier(item, TextureType.HANDLE_WRAPPING))
            .put(particleKey, getMaterialTextureIdentifier(item, TextureType.TOOL))

        // Create model with template parent and required texture keys
        val model = ModelTemplate(
            Optional.of(templateModelId),
            Optional.empty(),
            handleMaterialKey,
            toolMaterialKey,
            finishingMaterialKey,
            handleWrappingMaterialKey,
            particleKey
        )

        // "Bake" (upload) the model with texture map - generates final JSON
        val uploadedModelId = model.create(item, textureMap, generator.modelOutput)

        // Register finished model for the item
        generator.itemModelOutput.accept(item, ItemModelUtils.plainModel(uploadedModelId))
    }
}