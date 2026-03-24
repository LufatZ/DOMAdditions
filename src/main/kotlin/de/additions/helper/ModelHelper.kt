package de.additions.helper

import de.additions.datagen.ModelGenerator
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.GrassBlock
import net.minecraft.world.level.block.SnowLayerBlock
import net.minecraft.client.data.models.BlockModelGenerators
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.data.models.model.TextureSlot
import net.minecraft.client.data.models.model.TextureMapping
import net.minecraft.client.color.item.ItemTintSource
import net.minecraft.resources.Identifier

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
        block.defaultBlockState().blockHolder.registeredName
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
    }.let { Identifier.parse(it) }
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
    ): TextureMapping = TextureMapping().apply {
        val topIdentifier = buildTextureIdentifier(
            block = top,
            top = hasSideAndTop && top !is SnowLayerBlock,
            removeBlock = removeBlock,
            bottom = parent in ModelGenerator.Companion.bottomAllSide
        )

        val sideIdentifier = buildTextureIdentifier(
            block = side,
            side = hasSideAndTop && top !is SnowLayerBlock,
            removeBlock = removeBlock,
            snow = top is SnowLayerBlock && parent !is SnowLayerBlock
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
                put(TextureSlot.TEXTURE, topIdentifier)
            }

            "overlay" -> {
                put(TextureSlot.TOP, topIdentifier)
                put(TextureSlot.SIDE, sideIdentifier)
                put(TextureSlot.BOTTOM, bottomIdentifier)
                put(TextureSlot.LAYER0, overlayIdentifier)
            }

            else -> {
                put(TextureSlot.TOP, topIdentifier)
                put(TextureSlot.SIDE, sideIdentifier)
                put(TextureSlot.BOTTOM, bottomIdentifier)
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
        generator: BlockModelGenerators?
    ) {
        val grassColor: ItemTintSource = ItemModelUtils.constantTint(0x91BD59)
        when (parentBlock) {
            is GrassBlock -> generator?.registerSimpleTintedItemModel(block, model, grassColor)
            else -> generator?.registerSimpleItemModel(block, model)
        }
    }
}