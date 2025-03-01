package de.additions

import de.additions.blocks.BlockRegistry
import de.additions.config.AdditionsConfig
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.client.render.RenderLayer
import net.minecraft.state.property.Properties

object AdditionsClient : ClientModInitializer {
    override fun onInitializeClient() {
        textureCutOut()
        tintBlocks()
        val logging = AdditionsConfig.TranslationLogging || FabricLoader.getInstance().isDevelopmentEnvironment
        if (AdditionsConfig.EnabledTranslation) {
            val url =
                when (AdditionsConfig.TranslationUrl) {
                    AdditionsConfig.Companion.TranslationVersion.OXFATECH ->
                        "https://oxfatech.de/mod_translation/dayofmind-additions(latest).zip"
                    AdditionsConfig.Companion.TranslationVersion.CROWDIN ->
                        "https://crowdin.com/backend/download/project/dayofmind-addition.zip"
                    else -> AdditionsConfig.TranslationUrlCustom
                }
            runBlocking {
                TranslationManager.downloadTranslations(
                    url = url,
                    logging = logging,
                )
            }
        } else {
            TranslationManager.run {
                clearTranslations(logging)
            }
        }
    }

    private fun textureCutOut() {
        val lanterns: List<Block> = BlockRegistry.registeredLanterns.keys.toList()
        val blocksForTextureCutOut =
            BlockRegistry.registeredGrassBlocks +
                BlockRegistry.registeredChains +
                lanterns

        BlockRenderLayerMap.INSTANCE.putBlocks(
            RenderLayer.getCutoutMipped(),
            *blocksForTextureCutOut.toTypedArray(),
        )
    }

    private fun tintBlocks() {
        val blocksForTint = BlockRegistry.registeredGrassBlocks

        ColorProviderRegistry.BLOCK.register(
            { state, world, pos, tintIndex ->
                if (state.contains(Properties.SNOWY) && state.get(Properties.SNOWY) == false || !state.contains(Properties.SNOWY)) {
                    val biome =
                        world?.let { worldInstance ->
                            pos?.let { worldInstance.getBiomeFabric(it) }
                        }
                    biome?.value()?.getGrassColorAt(pos!!.x.toDouble(), pos.z.toDouble())
                        ?: 0x91BD59
                } else {
                    0xFFFFFF
                }
            },
            *blocksForTint.toTypedArray(),
        )
    }
}
