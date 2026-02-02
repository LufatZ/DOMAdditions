package de.additions.helper

import de.additions.datagen.ModelGenerator
import net.minecraft.block.Block
import net.minecraft.block.GrassBlock
import net.minecraft.block.SnowBlock
import net.minecraft.client.data.BlockStateModelGenerator
import net.minecraft.client.data.ItemModels
import net.minecraft.client.data.TextureKey
import net.minecraft.client.data.TextureMap
import net.minecraft.client.render.item.tint.TintSource
import net.minecraft.util.Identifier

object ModelHelper {
    /**
     * Extracts a clean block ID from a block's registry entry.
     *
     * Removes the namespace and optionally removes the "_block" suffix.
     *
     * @param block The block to extract ID from
     * @param removeBlock Whether to remove the "_block" suffix
     * @return Cleaned block identifier
     */
    fun extractCleanBlockIdentifier(block: Block, removeBlock: Boolean = false): String =
        block.defaultState.registryEntry.idAsString
            .replace("minecraft:", "")
            .let { if (removeBlock) it.replace("_block", "") else it }


    /**
     * Builds a full texture identifier for a block.
     *
     * Supports generating identifiers with various suffixes like "_side", "_top", etc.
     *
     * @param block The source block
     * @param side Whether to append "_side" suffix
     * @param top Whether to append "_top" suffix
     * @param removeBlock Whether to remove "_block" from the identifier
     * @param bottom Whether to append "_bottom" suffix
     * @return Full texture identifier
     */
    private fun buildTextureIdentifier(
        block: Block,
        side: Boolean = false,
        top: Boolean = false,
        removeBlock: Boolean = false,
        bottom: Boolean = false,
        overlay: Boolean = false,
        snow: Boolean = false
    ): Identifier = buildString {
        append("block/")
        append(extractCleanBlockIdentifier(block, removeBlock))
        if (top) append("_top")
        if (side) append("_side")
        if (bottom) append("_bottom")
        if (overlay) append("_overlay")
        if (snow) append("_snow")
    }.let { Identifier.of(it) }
    /**
     * Creates a TextureMap for a block with flexible texture configuration.
     *
     * Supports complex texture mapping scenarios like:
     * - Different textures for top, side, and bottom
     * - Handling blocks with special texture rules
     *
     * @param parent The primary block for texture reference
     * @param top Block used for top texture (defaults to parent)
     * @param side Block used for side texture (defaults to parent)
     * @param bottom Block used for bottom texture (defaults to parent)
     * @param hasSideAndTop Whether the block has distinct side and top textures
     * @param removeBlock Whether to remove "_block" from texture paths
     * @param bottomSameAsTop Whether bottom texture should match top texture
     * @param textureKey Specific texture key to use (defaults to ALL)
     * @return Configured TextureMap for model generation
     */
    fun configureBlockTextureMapping(
        parent: Block,
        top: Block = parent,
        side: Block = parent,
        bottom: Block = parent,
        hasSideAndTop: Boolean = false,
        removeBlock: Boolean = false,
        bottomSameAsTop: Boolean = false,
        textureKey: String = "default"
    ): TextureMap = TextureMap().apply {
        val topIdentifier = buildTextureIdentifier(
            block = top,
            top = hasSideAndTop && top !is SnowBlock,
            removeBlock = removeBlock,
            bottom = parent in ModelGenerator.Companion.bottomAllSide
        )

        val sideIdentifier = buildTextureIdentifier(
            block = side,
            side = hasSideAndTop && top !is SnowBlock,
            removeBlock = removeBlock,
            snow = top is SnowBlock && parent !is SnowBlock
        )

        val bottomIdentifier = buildTextureIdentifier(
            block = bottom,
            top = bottomSameAsTop,
            removeBlock = removeBlock
        )

        val overlayIdentifier = buildTextureIdentifier(
            block = parent,
            side = hasSideAndTop,
            overlay = true
        )

        when (textureKey) {
            "texture" -> {
                put(TextureKey.TEXTURE, topIdentifier)
            }

            "overlay" -> {
                put(TextureKey.TOP, topIdentifier)
                put(TextureKey.SIDE, sideIdentifier)
                put(TextureKey.BOTTOM, bottomIdentifier)
                put(TextureKey.LAYER0, overlayIdentifier)
            }

            else -> {
                put(TextureKey.TOP, topIdentifier)
                put(TextureKey.SIDE, sideIdentifier)
                put(TextureKey.BOTTOM, bottomIdentifier)
            }
        }
    }

    /**
     * Generates an item model for the given block.
     * Depending on the type of the parent block, the item model may be tinted.
     *
     * @param block The block for which the item model is to be generated.
     * @param parentBlock The parent block (e.g., if `block` is a DirtSlab, the `parentBlock` is Dirt).
     *                    Determines whether the model should be tinted.
     * @param model The identifier of the model to associate with the item.
     * @param generator The BlockStateModelGenerator responsible for registering block state models.
     *                  If null, no model will be registered.
     */
    fun generateBlockItemModel(
        block: Block,
        parentBlock: Block,
        model: Identifier,
        generator: BlockStateModelGenerator?
    ) {
        val grassColor: TintSource = ItemModels.constantTintSource(0x91BD59)
        when (parentBlock) {
            is GrassBlock -> generator?.registerTintedItemModel(block, model, grassColor)
            else -> generator?.registerParentedItemModel(block, model)
        }
    }
}