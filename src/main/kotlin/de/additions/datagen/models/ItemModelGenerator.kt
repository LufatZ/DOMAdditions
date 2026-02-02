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
import net.minecraft.block.Blocks
import net.minecraft.client.data.*
import net.minecraft.client.data.ItemModelGenerator
import net.minecraft.registry.tag.BlockTags
import net.minecraft.util.Identifier
import java.util.*

object ItemModelGenerator {

    /**
     * Generates all item models from registered items.
     */
    fun ItemModelGenerator?.generateItemsModels() {
        registeredItems.forEach { stack ->
            when (val item = stack.item) {
                is ToolItem -> generateMineToolModel(this, item)
                else -> logger.warn("Unknown item type: $item")
            }
        }
    }

    //TODO Needs simplifing, finetuning and documentation
    fun getHandleMaterialTextureIdentifier(item: ToolItem) : Identifier {
        var replace = listOf("","")
        val blockForTexture = when (item.material) {
            C_WOOD -> Blocks.STRIPPED_SPRUCE_WOOD.also { replace = listOf("wood", "log") }
            C_STONE -> Blocks.STRIPPED_SPRUCE_WOOD.also { replace = listOf("wood", "log") }
            C_IRON -> Blocks.ANVIL
            C_DIAMOND -> Blocks.ANVIL
            C_GOLD -> Blocks.ANVIL
            C_NETHERITE -> Blocks.NETHERITE_BLOCK
            else -> {
                logger.warn("$UNKNOWN_MATERIAL_MSG (from getHandleMaterialTextureIdentifier: ${item.material})")
                Blocks.OAK_PLANKS // Fallback block
            }
        }
        val id = Identifier.ofVanilla(
            "block/${ModelHelper.extractCleanBlockIdentifier(blockForTexture)
                .replace(replace[0], replace[1])}"
        )
        logger.info("Using material texture: $id for item: ${item.name}")
        return id
    }

    fun getToolMaterialTextureIdentifier(item: ToolItem): Identifier {
        var replace = listOf("", "")
        val blockForTexture = when (item.material) {
            C_WOOD -> Blocks.SPRUCE_LOG
            C_STONE -> Blocks.STONE
            C_IRON -> Blocks.IRON_BLOCK
            C_DIAMOND -> Blocks.DIAMOND_BLOCK
            C_GOLD -> Blocks.GOLD_BLOCK
            C_NETHERITE -> Blocks.NETHERITE_BLOCK
            else -> {
                logger.warn("$UNKNOWN_MATERIAL_MSG (from getToolMaterialTextureIdentifier: ${item.material})")
                Blocks.OAK_PLANKS // Fallback block
            }
        }
        val id = Identifier.ofVanilla(
            "block/${ModelHelper.extractCleanBlockIdentifier(blockForTexture)
                .replace(replace[0], replace[1])}"
        )
        logger.info("Using material texture: $id for item: ${item.name}")
        return id
    }

    fun getFinishingMaterialTextureIdentifier(item: ToolItem): Identifier {
        var replace = listOf("", "")
        val blockForTexture = when (item.material) {
            C_WOOD -> Blocks.STONE
            C_STONE -> Blocks.IRON_BLOCK
            C_IRON -> Blocks.OBSIDIAN
            C_DIAMOND -> Blocks.CRYING_OBSIDIAN
            C_GOLD -> Blocks.IRON_BLOCK
            C_NETHERITE -> Blocks.AMETHYST_BLOCK
            else -> {
                logger.warn("$UNKNOWN_MATERIAL_MSG (from getFinishingMaterialTextureIdentifier: ${item.material})")
                Blocks.OAK_PLANKS // Fallback block
            }
        }
        val id = Identifier.ofVanilla(
            "block/${ModelHelper.extractCleanBlockIdentifier(blockForTexture)
                .replace(replace[0], replace[1])}"
        )
        logger.info("Using material texture: $id for item: ${item.name}")
        return id
    }

    fun getHandleWrappingMaterialTextureIdentifier(item: ToolItem): Identifier {
        var replace = listOf("", "")
        val blockForTexture = when (item.material) {
            C_WOOD -> Blocks.GRAY_WOOL
            C_STONE -> Blocks.GRAY_WOOL
            C_IRON -> Blocks.GRAY_WOOL
            C_DIAMOND -> Blocks.GRAY_WOOL
            C_GOLD -> Blocks.GRAY_WOOL
            C_NETHERITE -> Blocks.GRAY_WOOL
            else -> {
                logger.warn("$UNKNOWN_MATERIAL_MSG (from getFinishingMaterialTextureIdentifier: ${item.material})")
                Blocks.OAK_PLANKS // Fallback block
            }
        }
        val id = Identifier.ofVanilla(
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
    private fun generateMineToolModel(generator: ItemModelGenerator?, item: ToolItem) {
        // Determine template model based on effective blocks
        val templateModelId = when(item.effectiveBlocks) {
            BlockTags.PICKAXE_MINEABLE ->
                if (item is RadiusMineItem)
                    Identifier.of(MODID, "item/template_hammer")
                else Identifier.of(MODID, "item/template_pickaxe")
            BlockTags.SHOVEL_MINEABLE -> Identifier.of(MODID, "item/template_shovel")
            BlockTags.AXE_MINEABLE -> Identifier.of(MODID, "item/template_axe")
            else -> Identifier.of(MODID, "item/template_hammer").also {
                logger.warn("Unknown effective block tag: ${item.effectiveBlocks}")
            }
        }

        // Define required texture keys (must use same instances)
        val handleMaterialKey = TextureKey.of("0")
        val toolMaterialKey = TextureKey.of("1")
        val finishingMaterialKey = TextureKey.of("2")
        val handleWrappingMaterialKey = TextureKey.of("3")
        val particleKey = TextureKey.of("particle")

        // Create texture map (template already defines the "stick" part via key "0")
        val textureMap = TextureMap()
            .put(handleMaterialKey, getHandleMaterialTextureIdentifier(item))
            .put(toolMaterialKey, getToolMaterialTextureIdentifier(item))
            .put(finishingMaterialKey, getFinishingMaterialTextureIdentifier(item))
            .put(handleWrappingMaterialKey, getHandleWrappingMaterialTextureIdentifier(item))
            .put(particleKey, getToolMaterialTextureIdentifier(item))

        // Create model with template parent and required texture keys
        val model = Model(
            Optional.of(templateModelId),
            Optional.empty(),
            handleMaterialKey,
            toolMaterialKey,
            finishingMaterialKey,
            handleWrappingMaterialKey,
            particleKey
        )

        // "Bake" (upload) the model with texture map - generates final JSON
        val uploadedModelId = model.upload(item, textureMap, generator?.modelCollector)

        // Register finished model for the item
        generator?.output?.accept(item, ItemModels.basic(uploadedModelId))
    }
}