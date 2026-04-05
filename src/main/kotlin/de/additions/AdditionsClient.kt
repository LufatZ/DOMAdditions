package de.additions

import de.additions.blocks.BlockRegistry
import de.additions.config.AdditionsConfig
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry.register
import net.minecraft.client.color.block.BlockTintSource
import net.minecraft.client.renderer.block.BlockAndTintGetter
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.BlockStateProperties

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
        tintBlocks()
        if (AdditionsConfig.EnabledTranslation) {
           ModTranslations.startTranslationDownload()
        }
    }

    /**
     * Registers a color provider for grass blocks to apply biome-specific tinting.
     * The color is determined by the biome's grass color, unless the block is snowy.
     * It checks if the call is for particles (world/pos is null) and returns default color then.
     * Otherwise, it only applies the tint if the tintIndex is 0 for block faces.
     */
    private fun tintBlocks() {
        val blocksForTint = BlockRegistry.registeredGrassBlocks

        register(
            listOf(
                object : BlockTintSource {
                    private fun isSnowy(state: BlockState) =
                        state.hasProperty(BlockStateProperties.SNOWY) && state.getValue(BlockStateProperties.SNOWY)

                    override fun color(state: BlockState): Int =
                        if (isSnowy(state)) 0xFFFFFF else 0x91BD59 // Fallback grass color

                    override fun colorInWorld(state: BlockState, level: BlockAndTintGetter, pos: BlockPos): Int =
                        if (isSnowy(state)) {
                            0xFFFFFF
                        } else {
                            val biome = level.getBiomeFabric(pos)
                            biome.value().getGrassColor(pos.x.toDouble(), pos.z.toDouble())
                        }
                }
            ),
            *blocksForTint.toTypedArray(),
        )
    }
}
