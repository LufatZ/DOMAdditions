package de.additions

import de.additions.blocks.BlockRegistry
import de.additions.config.AdditionsConfig
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap.putBlocks
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.renderer.chunk.ChunkSectionLayer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import kotlin.collections.toList
import kotlin.collections.toTypedArray

/**
 * Client-side initializer for the DayOfMind Additions mod.
 * Handles client-specific tasks like setting block render layers,
 * registering block color providers, and managing translations.
 */
object AdditionsClient : ClientModInitializer {
    /**
     * Initializes client-side features upon mod loading.
     * Calls methods to set render layers for cutout textures,
     * register block color providers for biome-specific tinting,
     * and handles the download or clearing of translations based on configuration.
     */
    override fun onInitializeClient() {
        textureCutOut()
        tintBlocks()
        if (AdditionsConfig.EnabledTranslation) {
           ModTranslations.startTranslationDownload()
        }
    }
    /**
     * Sets the render layer for blocks that require cutout textures.
     * This includes grass blocks, chains, and lanterns to ensure transparency is handled correctly.
     */
    private fun textureCutOut() {
        val lanterns: List<Block> = BlockRegistry.registeredLanterns.keys.toList()
        val blocksForTextureCutOut: List<Block> =
            BlockRegistry.registeredGrassBlocks +
                BlockRegistry.registeredChains +
                lanterns

        putBlocks(
            ChunkSectionLayer.CUTOUT,
            *blocksForTextureCutOut.toTypedArray(),
        )
    }

    /**
     * Registers a color provider for grass blocks to apply biome-specific tinting.
     * The color is determined by the biome's grass color, unless the block is snowy.
     * It checks if the call is for particles (world/pos is null) and returns default color then.
     * Otherwise, it only applies the tint if the tintIndex is 0 for block faces.
     */
    private fun tintBlocks() {
        val blocksForTint = BlockRegistry.registeredGrassBlocks

        ColorProviderRegistry.BLOCK.register(
            { state, world, pos, tintIndex ->
                // Check if this call is for particles (world or pos will be null based on BlockColors.getParticleColor)
                if (world == null || pos == null) {
                    -1 // Return default color (no tint) for particles
                } else if (tintIndex == 0) { // Otherwise, it's for rendering a block face in the world
                    // Check if the block state has the SNOWY property and if it's false,
                    // or if the block state does not have the SNOWY property at all.
                    if (state.hasProperty(BlockStateProperties.SNOWY) && !state.getValue(BlockStateProperties.SNOWY) || !state.hasProperty(
                            BlockStateProperties.SNOWY)
                    ) {
                        // world and pos are guaranteed non-null here
                        val biome = world.getBiomeFabric(pos) // Use Fabric API to get biome
                        // Get the grass color from the biome, or use a default green if unavailable.
                        biome.value().getGrassColor(pos.x.toDouble(), pos.z.toDouble())
                            ?: 0x91BD59 // Default grass color
                    } else {
                        // If snowy, use white color.
                        0xFFFFFF // White
                    }
                } else {
                    // If tintIndex is not 0 (and not a particle call), return default white color.
                    -1 // Use -1 for default/no tint
                }
            },
            *blocksForTint.toTypedArray(),
        )
    }
}
