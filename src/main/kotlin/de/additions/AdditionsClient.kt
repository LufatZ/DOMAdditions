package de.additions

import de.additions.blocks.BlockRegistry
import de.additions.datagen.ModelGenerator
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.minecraft.client.render.RenderLayer
import net.minecraft.state.property.Properties

/**
 * Client-side initialization for the Additions mod.
 * Handles texture cutouts and block tinting.
 *
 * @see ModelGenerator for tinted item generation
 */
object AdditionsClient : ClientModInitializer {
    override fun onInitializeClient() {
        textureCutOut()
        tintBlocks()
    }

    /**
     * Sets up cutout render layers for specific blocks that need transparency.
     */
    private fun textureCutOut() {
        val blocksForTextureCutOut =
            BlockRegistry.registeredGrassBlocks +
            BlockRegistry.registeredChains +
            BlockRegistry.registeredLanterns

        BlockRenderLayerMap.INSTANCE.putBlocks(
            RenderLayer.getCutoutMipped(),
            *blocksForTextureCutOut.toTypedArray()
        )
    }

    /**
     * Registers color providers for blocks that need biome-based tinting.
     */
    private fun tintBlocks() {
        val blocksForTint = BlockRegistry.registeredGrassBlocks

        ColorProviderRegistry.BLOCK.register(
            { state, world, pos, tintIndex ->
                if (state.contains(Properties.SNOWY) && state.get(Properties.SNOWY) == false || !state.contains(Properties.SNOWY)) {
                    val biome = world?.let { worldInstance ->
                        pos?.let { worldInstance.getBiomeFabric(it) }
                    }
                    biome?.value()?.getGrassColorAt(pos!!.x.toDouble(), pos.z.toDouble())
                        ?: 0x91BD59
                } else {
                    0xFFFFFF
                }
            },
            *blocksForTint.toTypedArray()
        )
    }
}